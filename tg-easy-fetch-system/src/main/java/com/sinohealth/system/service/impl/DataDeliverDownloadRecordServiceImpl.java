package com.sinohealth.system.service.impl;

import cn.hutool.core.io.FileUtil;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserFileAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.dao.TgDeliverDownloadRecordDAO;
import com.sinohealth.system.domain.TgDeliverDownloadRecord;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.DataDeliverDownloadRecordService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-28 10:13
 */
@Service
@RequiredArgsConstructor
public class DataDeliverDownloadRecordServiceImpl implements DataDeliverDownloadRecordService {

    private final TgDeliverDownloadRecordDAO deliverDownloadRecordDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final UserFileAssetsDAO userFileAssetsDAO;


    @Override
    public void saveDownloadRecords(DeliverRequestContextHolder holder) {
        List<ApplicationDataSource> dataSources = holder.getApplicationDataSources();
        // 根据交付类型
        DeliverResourceType type = holder.getType();
        List<Long> assetsIds = dataSources.stream().map(ApplicationDataSource::getAssetsId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserDataAssets> assetsMap = Lambda.queryMapIfExist(assetsIds,
                userDataAssetsDAO.getBaseMapper()::selectBatchIds, UserDataAssets::getId);
        switch (type) {
            case FILE:
                Map<Long, UserFileAssets> userAssetsMap = Lambda.queryMapIfExist(assetsIds,
                        userFileAssetsDAO.getBaseMapper()::selectBatchIds, UserFileAssets::getId);
                for (ApplicationDataSource ds : dataSources) {
                    UserFileAssets userFileAssets = userAssetsMap.get(ds.getAssetsId());
                    String name = Optional.ofNullable(userFileAssets).map(UserFileAssets::getName).orElse("");
                    String suffix = FileUtil.getSuffix(name);
                    if (StringUtils.isNotBlank(suffix)) {
                        holder.setType(DeliverResourceType.match(suffix));
                    }
                    saveDownloadRecords(holder, ds.getAssetsId(), ApplicationConst.AssetsIcon.FILE, name, assetsMap);
                }
                break;
            case IMAGE:
            case EXCEL:
            case CSV:
            case PDF:
//                doSaveDownloadRecords(holder, assetsMap);
                for (ApplicationDataSource ds : dataSources) {
                    UserDataAssets userDataAssets = assetsMap.get(ds.getAssetsId());
                    String name = Optional.ofNullable(userDataAssets).map(UserDataAssets::getProjectName).orElse("");
                    saveDownloadRecords(holder, ds.getAssetsId(), ApplicationConst.AssetsIcon.DATA, name, assetsMap);
                }
                for (CharAnalysisDataSource ds : holder.getCharAnalysisDataSources()) {
                    saveDownloadRecords(holder, ds.getArkbiId(), ApplicationConst.AssetsIcon.CHART, ds.getName(), assetsMap);
                }
                for (PanelDataSource ds : holder.getPanelDataSources()) {
                    saveDownloadRecords(holder, ds.getArkbiId(), ApplicationConst.AssetsIcon.DASHBOARD, ds.getName(), assetsMap);
                }
                break;
            default:
        }
    }

    private void saveDownloadRecords(DeliverRequestContextHolder requestContextHolder, Long id, String icon,
                                     String name, Map<Long, UserDataAssets> assetsMap) {

        DeliverDataSourceType type = DeliverDataSourceType.fromIcon(icon);
        Long tableId = null;
        String tableName = null;
        Long assetsId = null;
        if (Objects.equals(DeliverDataSourceType.ASSETS, type) && assetsMap.containsKey(id)) {
            UserDataAssets applyInfo = assetsMap.get(id);
            tableId = applyInfo.getBaseTableId();
            tableName = applyInfo.getBaseTableName();
            assetsId = applyInfo.getId();
        }
        TgDeliverDownloadRecord childRecord = new TgDeliverDownloadRecord();
        childRecord.setAssetsId(assetsId);
        childRecord.setTableId(tableId);
        childRecord.setTableName(tableName);
        childRecord.setAllocateType(BooleanUtils.isTrue(requestContextHolder.getPack()) ? 1 : 0);
        childRecord.setProjectName(name);
        childRecord.setPackName(null);
//        childRecord.setParentRecordId(parentRecord.getId());
        childRecord.setNodeId(id);
        childRecord.setIcon(icon);
        childRecord.setDownloadType(requestContextHolder.getType().name());
        childRecord.setDownloadTime(new Date());
        childRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        childRecord.setCreateTime(new Date());
        deliverDownloadRecordDAO.save(childRecord);
    }

    private void doSaveDownloadRecords(DeliverRequestContextHolder requestContextHolder, Map<Long, UserDataAssets> applicationInfoMap) {
        // 因为批量交付客户如果勾选了多个数据资产，则每个数据资产下都要保存交付记录
        for (ApplicationDataSource applicationDataSource : requestContextHolder.getApplicationDataSources()) {
            // 构建父节点
            TgDeliverDownloadRecord rootRecord = buildRootRecord(requestContextHolder, applicationDataSource, applicationInfoMap);
            // 再构建子节点， 子节点是所有类型的数据节点
            for (Map.Entry<DataDir, List<DataDir>> parentEntry : requestContextHolder.getDirMap().entrySet()) {
                DataDir parentDir = parentEntry.getKey();
                List<DataDir> sonDirs = parentEntry.getValue();
                TgDeliverDownloadRecord parentRecord = buildRecord(requestContextHolder, parentDir, rootRecord, applicationInfoMap);
                for (DataDir sonDir : sonDirs) {
                    buildRecord(requestContextHolder, sonDir, parentRecord, applicationInfoMap);
                }
            }
        }
    }

    private TgDeliverDownloadRecord buildRootRecord(DeliverRequestContextHolder requestContextHolder,
                                                    ApplicationDataSource applicationDataSource,
                                                    Map<Long, UserDataAssets> applicationInfoMap) {
        if (BooleanUtils.isFalse(requestContextHolder.getPack())) {
            TgDeliverDownloadRecord rootRecord = new TgDeliverDownloadRecord();
            rootRecord.setId(null);
            return rootRecord;
        }
        UserDataAssets applicationInfo = applicationInfoMap.get(applicationDataSource.getAssetsId());
        // 构建父节点, 父节点是打包节点
        TgDeliverDownloadRecord rootRecord = new TgDeliverDownloadRecord();
        rootRecord.setAssetsId(applicationDataSource.getAssetsId());
        rootRecord.setTableId(applicationInfo.getBaseTableId());
        rootRecord.setTableName(applicationInfo.getBaseTableName());
        rootRecord.setAllocateType(BooleanUtils.isTrue(requestContextHolder.getPack()) ? 1 : 0);
        rootRecord.setProjectName(requestContextHolder.getPackName());
        rootRecord.setPackName(requestContextHolder.getPackName());
        rootRecord.setParentRecordId(null);
        rootRecord.setNodeId(null);
        rootRecord.setIcon(CommonConstants.ICON_PACK);
        rootRecord.setDownloadType(requestContextHolder.getType().name());
        rootRecord.setDownloadTime(new Date());
        rootRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        rootRecord.setCreateTime(new Date());
        deliverDownloadRecordDAO.save(rootRecord);
        return rootRecord;
    }

    private TgDeliverDownloadRecord buildRecord(DeliverRequestContextHolder requestContextHolder,
                                                DataDir dataDir, TgDeliverDownloadRecord parentRecord,
                                                Map<Long, UserDataAssets> applicationInfoMap) {
        DeliverDataSourceType type = DeliverDataSourceType.fromIcon(dataDir.getIcon());
        Long tableId = null;
        String tableName = null;
        Long assetsId = null;
        if (Objects.equals(DeliverDataSourceType.ASSETS, type) && applicationInfoMap.containsKey(dataDir.getNodeId())) {
            UserDataAssets applyInfo = applicationInfoMap.get(dataDir.getNodeId());
            tableId = applyInfo.getBaseTableId();
            tableName = applyInfo.getBaseTableName();
            assetsId = applyInfo.getId();
        }
        TgDeliverDownloadRecord childRecord = new TgDeliverDownloadRecord();
        childRecord.setAssetsId(assetsId);
        childRecord.setTableId(tableId);
        childRecord.setTableName(tableName);
        childRecord.setAllocateType(BooleanUtils.isTrue(requestContextHolder.getPack()) ? 1 : 0);
        childRecord.setProjectName(dataDir.getDirName());
        childRecord.setPackName(null);
        childRecord.setParentRecordId(parentRecord.getId());
        childRecord.setNodeId(dataDir.getNodeId());
        childRecord.setIcon(dataDir.getIcon());
        childRecord.setDownloadType(requestContextHolder.getType().name());
        childRecord.setDownloadTime(new Date());
        childRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        childRecord.setCreateTime(new Date());
        deliverDownloadRecordDAO.save(childRecord);
        return childRecord;
    }
}
