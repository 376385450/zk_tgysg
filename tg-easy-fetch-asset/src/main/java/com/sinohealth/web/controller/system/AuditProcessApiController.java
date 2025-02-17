package com.sinohealth.web.controller.system;


import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.service.IAuditProcessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(value = "/api/table_management/process", tags = {"流程审核接口"})
@RestController
@RequestMapping("/api/table_management/process")
public class AuditProcessApiController extends BaseController {

    @Autowired
    private IAuditProcessService auditProcessService;


    
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "TgAuditProcessInfo", name = "auditProcessInfo", value = "")
    })
    @ApiOperation(value = "新建/修改流程", notes = "提数流程", httpMethod = "POST")
    @PostMapping("/add")
//    @RepeatSubmit
    public AjaxResult add(@RequestBody @Valid TgAuditProcessInfo auditProcessInfo) {
        auditProcessService.add(auditProcessInfo);
        return AjaxResult.success(auditProcessInfo);
    }

    
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "TgAuditProcessInfo", name = "auditProcessInfo", value = "")
    })
    @ApiOperation(value = "修改流程通用性", notes = "", httpMethod = "POST")
    @PostMapping("/update_generic")
    public AjaxResult updateGeneric(@RequestBody TgAuditProcessInfo auditProcessInfo) {

        auditProcessService.updateGeneric(auditProcessInfo);
        return AjaxResult.success("ok");
    }

    
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params", value = "")
    })
    @ApiOperation(value = "查询流程", notes = "", httpMethod = "GET")
    @GetMapping("/query")
    public AjaxResult query(@RequestParam Map<String, Object> params) {
        Object auditProcessInfos = auditProcessService.query(params);
        return AjaxResult.success(auditProcessInfos);
    }

    
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params", value = "")
    })
    @ApiOperation(value = "删除流程", notes = "", httpMethod = "DELETE")
    @DeleteMapping("/delete")
    public AjaxResult delete(@RequestParam Map<String, Object> params) {
        return auditProcessService.delete(params);
    }

}
