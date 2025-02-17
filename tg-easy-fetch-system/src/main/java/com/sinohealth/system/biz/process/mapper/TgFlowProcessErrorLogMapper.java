package com.sinohealth.system.biz.process.mapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.process.domain.TgFlowProcessErrorLog;

@Repository
@DataSource(DataSourceType.MASTER)
public interface TgFlowProcessErrorLogMapper extends BaseMapper<TgFlowProcessErrorLog> {

}
