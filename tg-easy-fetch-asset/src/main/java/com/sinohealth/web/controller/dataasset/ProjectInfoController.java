package com.sinohealth.web.controller.dataasset;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.dto.request.TagQueryRequest;
import com.sinohealth.system.biz.application.entity.ProjectInfoEntity;
import com.sinohealth.system.biz.application.service.CustomTagService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-10-17 19:43
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/pinfo"})
public class ProjectInfoController {

    @Autowired
    private CustomTagService customTagService;

    @GetMapping("/listProjectName")
    public AjaxResult<List<String>> listProjectName(String key) {
        return AjaxResult.success(customTagService.listProjectName(key));
    }

    @PostMapping("/listTag")
    public AjaxResult<List<String>> listTag(@RequestBody TagQueryRequest request) {
        if (CollectionUtils.isEmpty(request.getProjectNames())) {
            return AjaxResult.error("请先选择项目");
        }
        return AjaxResult.success(customTagService.listTag(request.getProjectNames(), request.getKey()));
    }

    @PostMapping("/listRelateInfos")
    public AjaxResult<ProjectInfoEntity> listRelateInfos(@RequestBody TagQueryRequest request) {
        if (CollectionUtils.isEmpty(request.getProjectNames()) || CollectionUtils.isEmpty(request.getTags())) {
            return AjaxResult.error("请先选择项目和标签");
        }

        return AjaxResult.success(customTagService.listRelateInfos(request.getProjectNames(), request.getTags()));
    }

}
