package com.sinohealth.system.biz.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfigSnapshot;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-05 15:38
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface ApplicationTaskConfigSnapshotMapper extends BaseMapper<ApplicationTaskConfigSnapshot> {
}
