package com.sinohealth.web.controller.catalogue;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.api.catalogue.CatalogueManageApi;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.system.dto.api.cataloguemanageapi.*;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@RestController
@RequestMapping({"/api/catalogue"})
@Slf4j
public class CatalogueManageApiController implements CatalogueManageApi{

    @Autowired
    DataAssetsCatalogueService dataAssetsCatalogueService;

    @Override
    @GetMapping("/getCatalogue")
    public AjaxResult<List<CatalogueQueryDTO>> getCatalogueTree() {
        List<CatalogueQueryDTO> list = dataAssetsCatalogueService.getCatalogueTree();
        return AjaxResult.success(list);
    }

    @GetMapping("/getCatalogueWithoutPermission")
    public AjaxResult<List<CatalogueAllDTO>> getCatalogueWithoutPermission() {
        List<CatalogueAllDTO> list = dataAssetsCatalogueService.getCatalogueWithoutPermission();
        return AjaxResult.success(list);
    }

    @Override
    @GetMapping("/getReadableCatalogue")
    public AjaxResult<CatalogueDataReadTree> getReadAbleCatalogue() {
        CatalogueDataReadTree tree = dataAssetsCatalogueService.getReadAbleCatalogue();
        return AjaxResult.success(tree);
    }


    @Override
    @GetMapping("/getAssetsManageCatalogue")
    public AjaxResult<List<CatalogueAssetManageAbleDTO>> getAssetsManageAbleCatalogue() {
        List<CatalogueAssetManageAbleDTO> list = dataAssetsCatalogueService.getAssetsManageAbleCatalogue();
        return AjaxResult.success(list);
    }

    /*    @Override
    @GetMapping("/getServiceFlow")
    public Result<List<ServiceFlowDTO>> getServiceFlow() {
        List<ServiceFlowDTO> list = dataAssetsCatalogueService.getServiceFlow();
        return Result.success(list);
    }*/

    @Override
    @GetMapping("/getCatalogueBaseInfo")
    public AjaxResult<CatalogueDetailDTO> getCatalogueBaseInfo(Integer id) {
        return AjaxResult.success(dataAssetsCatalogueService.getCatalogueBaseInfo(id));
    }

    @Override
    @GetMapping("/getInheritedPermissions")
    public AjaxResult<List<UserPermissionDTO>> getInheritedPermissions(Integer parentId) {
        return AjaxResult.success(dataAssetsCatalogueService.getInheritedPermissions(parentId));
    }

    @Override
    @GetMapping("/getUser")
    public AjaxResult<Page<UserDTO>> getSelectedUser(@RequestParam("pageNum") Integer pageNum,
                                                 @RequestParam("pageSize") Integer pageSize,
                                                 @RequestParam(value = "name",required = false) String name) {
        return AjaxResult.success(dataAssetsCatalogueService.getSelectedUser(pageNum, pageSize, name));
    }

    @Override
    @GetMapping("/getDept")
    public AjaxResult<List<DeptDTO>> getSelectedDept() {
        return AjaxResult.success(dataAssetsCatalogueService.getSelectedDept());
    }

    @Override
    @GetMapping("/getCataloguePermissions")
    public AjaxResult getCataloguePermissions(Integer id) {
        return null;
    }

    @Override
    @PostMapping("/saveOrUpdate")
    public AjaxResult<Integer> saveOrUpdateCatalogue(@RequestBody CatalogueDTO dto) {
        return AjaxResult.success(dataAssetsCatalogueService.saveOrUpdate(dto));
    }

    @Override
    @DeleteMapping("/deleteCatalogue")
    public AjaxResult deleteCatalogue(@RequestParam("id") Integer id) {
        dataAssetsCatalogueService.deleteCatalogue(id);
        return AjaxResult.success();
    }
}
