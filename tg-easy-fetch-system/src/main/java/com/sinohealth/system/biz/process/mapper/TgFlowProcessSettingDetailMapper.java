package com.sinohealth.system.biz.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;
import org.springframework.stereotype.Repository;

@Repository
@DataSource(DataSourceType.MASTER)
public interface TgFlowProcessSettingDetailMapper extends BaseMapper<TgFlowProcessSettingDetail> {
}
