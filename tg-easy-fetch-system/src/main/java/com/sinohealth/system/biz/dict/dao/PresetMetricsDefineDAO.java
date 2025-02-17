package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.PresetMetricsDefine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-29 19:56
 */
public interface PresetMetricsDefineDAO extends IService<PresetMetricsDefine> {

    List<PresetMetricsDefine> queryByMetricsId(Collection<Long> metricsIds);
    Map<Long, List<PresetMetricsDefine>> queryByPresetMetricsId(Collection<Long> presetIds);


}
