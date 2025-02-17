package com.sinohealth.system.service.impl;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.dao.TgDeliverCustomerRecordDAO;
import com.sinohealth.system.domain.TgDeliverCustomerRecord;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.dto.CustomerApplyAuthReqV2DTO;
import com.sinohealth.system.dto.CustomerApplyAuthRequestContext;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataDeliverCustomerRecordService;
import com.sinohealth.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-09 15:02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDeliverCustomerRecordServiceImpl implements DataDeliverCustomerRecordService {

    private final TgDeliverCustomerRecordDAO tgDeliverCustomerRecordDAO;

    private final TgApplicationInfoMapper applicationInfoMapper;

    private final UserDataAssetsDAO userDataAssetsDAO;

    private final ISysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApplyCustomerRecords(CustomerApplyAuthReqV2DTO authReqDTO, CustomerApplyAuthRequestContext authRequestContext) {
        // 记录是跟表单绑定的，必须要有表单数据
        if (CollectionUtils.isEmpty(authRequestContext.getApplicationDataSources())) {
            return ;
        }
        List<ApplicationDataSource> applicationDataSources = authRequestContext.getApplicationDataSources();

        List<Long> assetsIds = applicationDataSources.stream().map(ApplicationDataSource::getAssetsId).distinct().collect(Collectors.toList());

        Map<Long, UserDataAssets> assetsMap = userDataAssetsDAO.getBaseMapper().selectBatchIds(assetsIds).stream()
                .collect(Collectors.toMap(UserDataAssets::getId, Function.identity()));
        CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser = authRequestContext.getAuthList().get(0);
        SysUser sysUser = sysUserService.getById(authUser.getUserId());
        // 因为批量交付客户如果勾选了多个数据资产，则每个数据资产下都要保存交付记录
        for (ApplicationDataSource applicationDataSource : applicationDataSources) {
            // 构建父节点
            TgDeliverCustomerRecord rootRecord = this.buildRootRecord(authRequestContext, applicationDataSource, assetsMap, authUser, sysUser);
            // 再构建子节点， 子节点是所有类型的数据节点
            for (Map.Entry<DataDir, List<DataDir>> parentEntry : authRequestContext.getDirMap().entrySet()) {
                DataDir parentDir = parentEntry.getKey();
                List<DataDir> sonDirs = parentEntry.getValue();
                TgDeliverCustomerRecord parentRecord = this.buildRecord(authRequestContext, parentDir, rootRecord, assetsMap, authUser, sysUser);
                for (DataDir sonDir : sonDirs) {
                    this.buildRecord(authRequestContext, sonDir, parentRecord, assetsMap, authUser, sysUser);
                }
            }
        }
    }
    private TgDeliverCustomerRecord buildRootRecord(CustomerApplyAuthRequestContext authRequestContext,
                                                    ApplicationDataSource applicationDataSource, Map<Long, UserDataAssets> applicationInfoMap,
                                                    CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser, SysUser sysUser) {
        if (BooleanUtils.isFalse(authRequestContext.getPack())) {
            TgDeliverCustomerRecord rootRecord = new TgDeliverCustomerRecord();
            rootRecord.setId(null);
            return rootRecord;
        }
        UserDataAssets applicationInfo = applicationInfoMap.get(applicationDataSource.getAssetsId());
        // 构建父节点, 父节点是打包节点
        TgDeliverCustomerRecord rootRecord = new TgDeliverCustomerRecord();
        rootRecord.setAssetsId(applicationDataSource.getAssetsId());
        rootRecord.setTableId(applicationInfo.getBaseTableId());
        rootRecord.setTableName(applicationInfo.getBaseTableName());
        rootRecord.setAllocateType(BooleanUtils.isTrue(authRequestContext.getPack()) ? 1 : 0);
        rootRecord.setAllocateUserId(authUser.getUserId());
        rootRecord.setAllocateUserName(sysUser.getUserName());
        rootRecord.setAuthType(authUser.getAuthType());
        rootRecord.setProjectName(authRequestContext.getPackName());
        rootRecord.setPackName(authRequestContext.getPackName());
        rootRecord.setParentRecordId(null);
        rootRecord.setNodeId(null);
        rootRecord.setIcon(CommonConstants.ICON_PACK);
        rootRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        rootRecord.setCreateTime(new Date());
        tgDeliverCustomerRecordDAO.save(rootRecord);
        return rootRecord;
    }

    private TgDeliverCustomerRecord buildRecord(CustomerApplyAuthRequestContext authRequestContext,
                                                DataDir dataDir,
                                                TgDeliverCustomerRecord parentRecord,
                                                Map<Long, UserDataAssets> applicationInfoMap,
                                                CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser,
                                                SysUser sysUser) {
        DeliverDataSourceType type = DeliverDataSourceType.fromIcon(dataDir.getIcon());
        Long tableId = null;
        String tableName = null;
        Long assetsId = null;
        if (Objects.equals(DeliverDataSourceType.ASSETS, type) && applicationInfoMap.containsKey(dataDir.getNodeId())) {
            UserDataAssets assets = applicationInfoMap.get(dataDir.getNodeId());
            tableId = assets.getBaseTableId();
            tableName = assets.getBaseTableName();
            assetsId = assets.getId();
        }
        TgDeliverCustomerRecord childRecord = new TgDeliverCustomerRecord();
        childRecord.setAssetsId(assetsId);
        childRecord.setTableId(tableId);
        childRecord.setTableName(tableName);
        childRecord.setAllocateType(BooleanUtils.isTrue(authRequestContext.getPack()) ? 1 : 0);
        childRecord.setAllocateUserId(authUser.getUserId());
        childRecord.setAllocateUserName(sysUser.getUserName());
        childRecord.setAuthType(authUser.getAuthType());
        childRecord.setProjectName(dataDir.getDirName());
        childRecord.setPackName(null);
        childRecord.setParentRecordId(parentRecord.getId());
        childRecord.setNodeId(dataDir.getNodeId());
        childRecord.setIcon(dataDir.getIcon());
        childRecord.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
        childRecord.setCreateTime(new Date());
        tgDeliverCustomerRecordDAO.save(childRecord);
        return childRecord;
    }

}
