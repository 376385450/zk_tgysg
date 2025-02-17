package com.sinohealth.system.biz.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.project.domain.Project;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:31
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface ProjectMapper extends BaseMapper<Project> {

}
