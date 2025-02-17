package com.sinohealth.system.biz.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-25 17:50
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface ApplicationTaskConfigMapper extends BaseMapper<ApplicationTaskConfig> {

}
