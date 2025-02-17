package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordQueryRequest;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordStatusParam;
import com.sinohealth.system.mapper.ApplicationDataUpdateRecordMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-08 17:54
 */
@Repository
public class ApplicationDataUpdateRecordDAOImpl
        extends ServiceImpl<ApplicationDataUpdateRecordMapper, ApplicationDataUpdateRecord>
        implements ApplicationDataUpdateRecordDAO {

    public List<ApplicationDataUpdateRecord> queryByAssetIds(Collection<Long> assetIds) {
        if (CollectionUtils.isEmpty(assetIds)) {
            return Collections.emptyList();
        }

        return this.baseMapper.selectList(new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                        .in(ApplicationDataUpdateRecord::getAssetsId, assetIds)
//                .last(" LIMIT 500")
        );
    }

    @Override
    public List<ApplicationDataUpdateRecord> queryCustomerByApplyIds(Collection<Long> assetIds) {
        if (CollectionUtils.isEmpty(assetIds)) {
            return Collections.emptyList();
        }

        return this.baseMapper.selectList(new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                        .eq(ApplicationDataUpdateRecord::getSyncTarget, SyncTargetType.CUSTOMER_DS)
                        .in(ApplicationDataUpdateRecord::getAssetsId, assetIds)
//                .last(" LIMIT 500")
        );
    }

    @Override
    public Map<Long, ApplicationDataUpdateRecord> queryLatestByAssetIds(Collection<Long> assetIds) {
        List<ApplicationDataUpdateRecord> updateRecords = this.queryByAssetIds(assetIds);
        return updateRecords.stream()
                .collect(Collectors.toMap(ApplicationDataUpdateRecord::getAssetsId, v -> v, (front, current) -> {
                    if (front.getCreateTime().before(current.getCreateTime())) {
                        return current;
                    }
                    return front;
                }));
    }

    /**
     * @see AssetsVersion#getAssetsVersion()
     */
    @Override
    public Map<String, ApplicationDataUpdateRecord> queryLatestByAssetVersions(Collection<String> assetVersions) {
        if (CollectionUtils.isEmpty(assetVersions)) {
            return Collections.emptyMap();
        }

        List<ApplicationDataUpdateRecord> updateRecords = this.baseMapper.selectList(new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                .in(ApplicationDataUpdateRecord::getAssetsVersion, assetVersions)
        );
        return updateRecords.stream()
                .collect(Collectors.toMap(ApplicationDataUpdateRecord::getAssetsVersion, v -> v, (front, current) -> {
                    if (front.getCreateTime().before(current.getCreateTime())) {
                        return current;
                    }
                    return front;
                }));
    }

    @Override
    public IPage<ApplicationDataUpdateRecord> pageQueryByAppId(UpdateRecordQueryRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getAssetsId())) {
            return new Page<>();
        }
        return this.baseMapper.selectPage(request.buildPage(),
                new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                        .eq(ApplicationDataUpdateRecord::getAssetsId, request.getAssetsId()));
    }

    @Override
    public ApplicationDataUpdateRecord querySyncApplication(UpdateRecordStatusParam param) {
        List<ApplicationDataUpdateRecord> records = this.baseMapper
                .selectList(new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                        .eq(ApplicationDataUpdateRecord::getAssetsId, param.getAssetId())
                        .eq(Objects.nonNull(param.getVersion()), ApplicationDataUpdateRecord::getVersion, param.getVersion())
                        .eq(Objects.nonNull(param.getSyncTarget()), ApplicationDataUpdateRecord::getSyncTarget, param.getSyncTarget())
                        .eq(Objects.nonNull(param.getUpdateState()), ApplicationDataUpdateRecord::getUpdateState, param.getUpdateState())
                        .in(CollectionUtils.isNotEmpty(param.getUpdateStates()), ApplicationDataUpdateRecord::getUpdateState, param.getUpdateStates())
                        .orderByDesc(ApplicationDataUpdateRecord::getCreateTime)
                        .last(" limit 1")
                );
        int size = CollectionUtils.size(records);
        if (size == 0) {
            return null;
        }
        return records.get(0);
    }

}
