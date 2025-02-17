package com.sinohealth.system.service.impl;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.dao.TgDeliverEmailRecordDAO;
import com.sinohealth.system.domain.TgDeliverEmailRecord;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverEmailRequest;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataDeliverEmailRecordService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-28 10:16
 */
@Service
@RequiredArgsConstructor
public class DataDeliverEmailRecordServiceImpl implements DataDeliverEmailRecordService {

    private final TgApplicationInfoMapper applicationInfoMapper;

    private final TgDeliverEmailRecordDAO deliverEmailRecordDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;

    @Override
    public void saveSendEmailRecords(ApplicationDeliverEmailRequest emailRequest, DeliverRequestContextHolder requestContextHolder) {
        List<ApplicationDataSource> dataSources = requestContextHolder.getApplicationDataSources();
        if (CollectionUtils.isEmpty(dataSources)) {
            return;
        }
        List<Long> assetsIds = dataSources.stream().map(ApplicationDataSource::getAssetsId).distinct().collect(Collectors.toList());
        List<UserDataAssets> assetsList = userDataAssetsDAO.getBaseMapper().selectBatchIds(assetsIds);

        Map<Long, UserDataAssets> applicationInfoMap = assetsList.stream()
                .collect(Collectors.toMap(UserDataAssets::getId, Function.identity()));
        // 因为批量交付客户如果勾选了多个数据资产，则每个数据资产下都要保存交付记录
        for (ApplicationDataSource applicationDataSource : requestContextHolder.getApplicationDataSources()) {
            // 构建父节点
            TgDeliverEmailRecord rootRecord = buildRootRecord(emailRequest, requestContextHolder, applicationDataSource, applicationInfoMap);
            // 再构建子节点， 子节点是所有类型的数据节点
            for (Map.Entry<DataDir, List<DataDir>> parentEntry : requestContextHolder.getDirMap().entrySet()) {
                DataDir parentDir = parentEntry.getKey();
                List<DataDir> sonDirs = parentEntry.getValue();
                TgDeliverEmailRecord parentRecord = buildRecord(emailRequest, requestContextHolder, parentDir, rootRecord, applicationInfoMap);
                for (DataDir sonDir : sonDirs) {
                    buildRecord(emailRequest, requestContextHolder, sonDir, parentRecord, applicationInfoMap);
                }
            }
        }
    }

    private TgDeliverEmailRecord buildRootRecord(ApplicationDeliverEmailRequest emailRequest,
                                                 DeliverRequestContextHolder requestContextHolder,
                                                 ApplicationDataSource applicationDataSource,
                                                 Map<Long, UserDataAssets> applicationInfoMap) {
        if (BooleanUtils.isFalse(requestContextHolder.getPack())) {
            TgDeliverEmailRecord rootRecord = new TgDeliverEmailRecord();
            rootRecord.setId(null);
            return rootRecord;
        }
        UserDataAssets applicationInfo = applicationInfoMap.get(applicationDataSource.getAssetsId());
        // 构建父节点, 父节点是打包节点
        TgDeliverEmailRecord rootRecord = new TgDeliverEmailRecord();
        rootRecord.setAssetsId(applicationDataSource.getAssetsId());
        rootRecord.setTableId(applicationInfo.getBaseTableId());
        rootRecord.setTableName(applicationInfo.getBaseTableName());
        rootRecord.setAllocateType(BooleanUtils.isTrue(requestContextHolder.getPack()) ? 1 : 0);
        rootRecord.setProjectName(requestContextHolder.getPackName());
        rootRecord.setPackName(requestContextHolder.getPackName());
        rootRecord.setParentRecordId(null);
        rootRecord.setNodeId(null);
        rootRecord.setIcon(CommonConstants.ICON_PACK);
        rootRecord.setTitle(emailRequest.getEmailTitle());
        rootRecord.setContent(emailRequest.getEmailBody());
        rootRecord.setReceiver(emailRequest.getEmailReceivers());
        rootRecord.setSendTime(new Date());
        rootRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        rootRecord.setCreateTime(new Date());
        deliverEmailRecordDAO.save(rootRecord);
        return rootRecord;
    }

    private TgDeliverEmailRecord buildRecord(ApplicationDeliverEmailRequest emailRequest,
                                             DeliverRequestContextHolder requestContextHolder,
                                             DataDir dataDir, TgDeliverEmailRecord parentRecord,
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
        TgDeliverEmailRecord childRecord = new TgDeliverEmailRecord();
        childRecord.setAssetsId(assetsId);
        childRecord.setTableId(tableId);
        childRecord.setTableName(tableName);
        childRecord.setAllocateType(BooleanUtils.isTrue(requestContextHolder.getPack()) ? 1 : 0);
        childRecord.setProjectName(dataDir.getDirName());
        childRecord.setPackName(null);
        childRecord.setParentRecordId(parentRecord.getId());
        childRecord.setNodeId(dataDir.getNodeId());
        childRecord.setIcon(dataDir.getIcon());
        childRecord.setTitle(emailRequest.getEmailTitle());
        childRecord.setContent(emailRequest.getEmailBody());
        childRecord.setReceiver(emailRequest.getEmailReceivers());
        childRecord.setSendTime(new Date());
        childRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        childRecord.setCreateTime(new Date());
        deliverEmailRecordDAO.save(childRecord);
        return childRecord;
    }
}
