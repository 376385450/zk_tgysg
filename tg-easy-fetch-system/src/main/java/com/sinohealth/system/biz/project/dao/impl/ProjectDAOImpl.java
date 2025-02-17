package com.sinohealth.system.biz.project.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:32
 */
@Repository
public class ProjectDAOImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectDAO {

}
