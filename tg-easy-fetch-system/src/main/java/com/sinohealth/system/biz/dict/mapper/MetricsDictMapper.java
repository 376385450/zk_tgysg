package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:16
 */
@Mapper
@DataSource(DataSourceType.MASTER)
public interface MetricsDictMapper extends BaseMapper<MetricsDict> {

    @Select("select max(sort) from tg_metrics_dict")
    Integer queryMaxSort();
}
