package com.sinohealth.web.controller.system;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.dto.MyViewDto;
import com.sinohealth.system.dto.QueryTableHistoryDto;
import com.sinohealth.system.dto.TableStatisticDto;
import com.sinohealth.system.dto.UpdateTableDto;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Api(tags = {"我的概览"})
@RestController
@RequestMapping("/system/view")
public class ViewController {
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ITableLogService tableLogService;


    @GetMapping("/statistics")
    //@ApiOperation(value = "我的预览-统计数量", response = MyViewDto.class)
    public AjaxResult myview() {
        return AjaxResult.success(tableLogService.getMyStatistics(SecurityUtils.getUserId()));
    }

    @GetMapping("/queryTableHistory")
    //@ApiOperation(value = "我的预览-最近打开表单", response = QueryTableHistoryDto.class)
    public AjaxResult queryTableCently() {
        return AjaxResult.success(tableLogService.getMyQueryTableHistory());
    }

    @GetMapping("/concernTableUpate")
    //@ApiOperation(value = "我的预览-关注表单更新", response = UpdateTableDto.class)
    public AjaxResult concernTableUpate() {
        return AjaxResult.success(tableLogService.getMyConcernTableTop20(SecurityUtils.getUserId()));
    }

    @GetMapping("/data/statistics")
    //@ApiOperation(value = "资产概览-内容概览统计", response = TableStatisticDto.class)
    public AjaxResult dataStatic(@RequestParam(required = false) Long dirId) {
        return AjaxResult.success(tableInfoService.getTableStatisticDto(dirId));
    }


}
