package com.sinohealth.system.biz.dict.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dict.dao.PresetMetricsDefineDAO;
import com.sinohealth.system.biz.dict.domain.PresetMetricsDefine;
import com.sinohealth.system.biz.dict.mapper.PresetMetricsDefineMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-29 19:57
 */
@Repository
public class PresetMetricsDefineDAOImpl
        extends ServiceImpl<PresetMetricsDefineMapper, PresetMetricsDefine>
        implements PresetMetricsDefineDAO {

    @Override
    public List<PresetMetricsDefine> queryByMetricsId(Collection<Long> metricsIds) {
        return baseMapper.selectList(new QueryWrapper<PresetMetricsDefine>()
                .lambda().in(PresetMetricsDefine::getMetricsId, metricsIds));
    }

    @Override
    public Map<Long, List<PresetMetricsDefine>> queryByPresetMetricsId(Collection<Long> presetIds) {
        List<PresetMetricsDefine> defines = baseMapper.selectList(new QueryWrapper<PresetMetricsDefine>()
                .lambda().in(PresetMetricsDefine::getPresetId, presetIds));
        return defines.stream().collect(Collectors.groupingBy(PresetMetricsDefine::getPresetId));
    }
}
