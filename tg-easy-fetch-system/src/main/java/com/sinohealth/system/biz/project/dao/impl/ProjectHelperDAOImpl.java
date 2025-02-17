package com.sinohealth.system.biz.project.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.domain.ProjectHelper;
import com.sinohealth.system.mapper.ProjectHelperMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 11:43
 */
@Repository
public class ProjectHelperDAOImpl extends ServiceImpl<ProjectHelperMapper, ProjectHelper> implements ProjectHelperDAO {

    public Set<Long> queryProjects(Long userId) {
        List<ProjectHelper> relateProjectList = baseMapper.selectList(new QueryWrapper<ProjectHelper>().lambda()
                .eq(ProjectHelper::getUserId, userId));
        if (CollectionUtils.isEmpty(relateProjectList)) {
            return Collections.emptySet();
        }

        return Lambda.buildSet(relateProjectList, ProjectHelper::getProjectId);
    }
}
