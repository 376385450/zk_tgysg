package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.PresetMetricsDefine;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-29 19:56
 */
@Mapper
@DataSource(DataSourceType.MASTER)
public interface PresetMetricsDefineMapper extends BaseMapper<PresetMetricsDefine> {
}
