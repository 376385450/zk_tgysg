package com.sinohealth.system.biz.project.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.ProjectHelper;

import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 11:42
 */
public interface ProjectHelperDAO extends IService<ProjectHelper> {

    Set<Long> queryProjects(Long userId);
}
