package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 09:40
 */
@Mapper
@DataSource(DataSourceType.MASTER)
public interface TgTableApplicationMappingInfoMapper extends BaseMapper<TgTableApplicationMappingInfo> {
}
