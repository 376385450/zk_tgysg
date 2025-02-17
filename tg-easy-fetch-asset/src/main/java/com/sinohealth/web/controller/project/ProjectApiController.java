package com.sinohealth.web.controller.project;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.project.dto.ProjectDTO;
import com.sinohealth.system.biz.project.dto.ProjectValDTO;
import com.sinohealth.system.biz.project.dto.request.ProjectUpsertParam;
import com.sinohealth.system.biz.project.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:34
 */
@Api(tags = {"项目管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/project")
public class ProjectApiController {

    private final ProjectService projectService;

    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    public AjaxResult<IPage<ProjectDTO>> pageQuery(@RequestBody @Validated DictCommonPageRequest request) {
        return projectService.pageQuery(request);
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取详情")
    public AjaxResult<ProjectDTO> detail(@RequestParam("id") Long id) {
        return projectService.detail(id);
    }

    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    public AjaxResult<Void> upsert(@RequestBody @Validated ProjectUpsertParam request) {
        return projectService.upsert(request);
    }

    @PostMapping("/changeStatus")
    @ApiOperation(value = "修改项目上下线状态")
    public AjaxResult<Void> changeStatus(@RequestBody ProjectUpsertParam request) {
        return projectService.changeStatus(request);
    }

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return projectService.deleteById(id);
    }

    /**
     * 1. 重新申请，新申请 项目下拉
     * 2. 另存为项目下拉
     */
    @GetMapping("/listAvailable")
    AjaxResult<List<ProjectValDTO>> listAvailableProjects(@RequestParam(value = "assetsId", required = false) Long assetsId) {
        return projectService.listAvailableProjects(assetsId);
    }

}
