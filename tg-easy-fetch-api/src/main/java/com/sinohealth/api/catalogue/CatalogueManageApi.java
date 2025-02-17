package com.sinohealth.api.catalogue;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.system.dto.api.cataloguemanageapi.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping({"/api/catalogue"})
public interface CatalogueManageApi {

    //@Operation(summary = "获取目录树")
    @GetMapping("/getCatalogue")
    AjaxResult<List<CatalogueQueryDTO>> getCatalogueTree();

    @GetMapping("/getReadableCatalogue")
    AjaxResult<CatalogueDataReadTree> getReadAbleCatalogue();

    @GetMapping("/getAssetsManageCatalogue")
    AjaxResult<List<CatalogueAssetManageAbleDTO>> getAssetsManageAbleCatalogue();

    //@Operation(summary = "获取类目基础信息")
    @GetMapping("/getCatalogueBaseInfo")
    AjaxResult<CatalogueDetailDTO> getCatalogueBaseInfo(@RequestParam("id") Integer id);

    //@Operation(summary = "查看继承权限")
    @GetMapping("/getInheritedPermissions")
    AjaxResult<List<UserPermissionDTO>> getInheritedPermissions(@RequestParam(value = "parentId",required = false) Integer getParentId);

    @GetMapping("/getUser")
    AjaxResult<Page<UserDTO>> getSelectedUser(@RequestParam("pageNum") Integer pageNum,
                                          @RequestParam("pageSize") Integer pageSize,
                                          @RequestParam(value = "name",required = false) String name);

    @GetMapping("/getDept")
    AjaxResult<List<DeptDTO>> getSelectedDept();

    //@Operation(summary = "获取权限与流程")
    @GetMapping("/getCataloguePermissions")
    AjaxResult getCataloguePermissions(@RequestParam("id") Integer id);

    //@Operation(summary = "新增或编辑类目")
    @PostMapping("/saveOrUpdate")
    AjaxResult<Integer> saveOrUpdateCatalogue(@RequestBody CatalogueDTO dto);

    //@Operation(summary = "删除类目")
    @DeleteMapping("/deleteCatalogue")
    AjaxResult deleteCatalogue(@RequestParam("id") Integer id);
}
