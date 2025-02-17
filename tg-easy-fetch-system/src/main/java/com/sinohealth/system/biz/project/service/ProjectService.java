package com.sinohealth.system.biz.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.project.dto.ProjectDTO;
import com.sinohealth.system.biz.project.dto.ProjectValDTO;
import com.sinohealth.system.biz.project.dto.request.ProjectUpsertParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:33
 */
public interface ProjectService {

    AjaxResult<IPage<ProjectDTO>> pageQuery(DictCommonPageRequest request);

    AjaxResult<ProjectDTO> detail(Long id);

    AjaxResult<Void> upsert(ProjectUpsertParam request);

    AjaxResult<Void> changeStatus(ProjectUpsertParam request);

    AjaxResult<Void> deleteById(Long id);

    AjaxResult<List<ProjectValDTO>> listAvailableProjects(Long assetsId);

    /**
     * 追加申请人到项目内
     *
     * @param userList projectId -> userId
     */
    void patchUserProjectRelation(Map<Long, Set<Long>> userList);
}
