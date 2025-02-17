package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.dao.AcceptanceRecordDAO;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;
import com.sinohealth.system.biz.dataassets.mapper.AcceptanceRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:34
 */

@Slf4j
@Repository
public class AcceptanceRecordDAOImpl
        extends ServiceImpl<AcceptanceRecordMapper, AcceptanceRecord>
        implements AcceptanceRecordDAO {

    /**
     * @return id#version -> record
     */
    @Override
    public Map<String, AcceptanceRecord> queryByAssetsIdAndVersion(Collection<? extends AssetsVersion> assetsList) {
        if (CollectionUtils.isEmpty(assetsList)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<AcceptanceRecord> wrapper = new QueryWrapper<AcceptanceRecord>().lambda()
                .select(AcceptanceRecord::getAssetsId, AcceptanceRecord::getVersion,
                        AcceptanceRecord::getState, AcceptanceRecord::getUser, AcceptanceRecord::getAssetsVersion);

        Set<String> bizIds = assetsList.stream().map(AssetsVersion::getAssetsVersion).collect(Collectors.toSet());
        wrapper.in(AcceptanceRecord::getAssetsVersion, bizIds);
//        for (AssetsVersion assets : assetsList) {
//            wrapper.or(v -> v.eq(AcceptanceRecord::getAssetsId, assets.getAssetsId()).eq(AcceptanceRecord::getVersion, assets.getVersion()));
//        }

        List<AcceptanceRecord> records = baseMapper.selectList(wrapper);
        return records.stream().collect(Collectors.toMap(AcceptanceRecord::getAssetsVersion, v -> v, (front, current) -> current));
    }

    @Override
    public Map<Long, AcceptanceRecord> queryLatestStateByApplyId(Collection<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<AcceptanceRecord> wrapper = new QueryWrapper<AcceptanceRecord>().lambda()
                .in(AcceptanceRecord::getApplicationId, applyIds);
        List<AcceptanceRecord> records = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }

        Map<Long, List<AcceptanceRecord>> applyMap = records.stream().collect(Collectors.groupingBy(AcceptanceRecord::getApplicationId));
        Map<Long, AcceptanceRecord> result = new HashMap<>();
        for (Map.Entry<Long, List<AcceptanceRecord>> entry : applyMap.entrySet()) {
            List<AcceptanceRecord> value = entry.getValue();
            Optional<AcceptanceRecord> maxOpt = value.stream().max(Comparator.comparing(AcceptanceRecord::getCreateTime));
            maxOpt.ifPresent(v -> result.put(entry.getKey(), v));
        }

        return result;
    }
}
