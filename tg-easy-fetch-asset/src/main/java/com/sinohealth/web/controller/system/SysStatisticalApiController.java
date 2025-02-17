package com.sinohealth.web.controller.system;

import com.sinohealth.common.annotation.Log;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.enums.BusinessType;
import com.sinohealth.common.enums.StatisticsPeriodType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.poi.ExcelUtil;
import com.sinohealth.system.domain.SysStatisticalRules;
import com.sinohealth.system.dto.StatisticalInfoDto;
import com.sinohealth.system.dto.SysStatisticalRulesDto;
import com.sinohealth.system.dto.SysStatisticalTableDto;
import com.sinohealth.system.service.ISysStatisticalRulesService;
import com.sinohealth.system.vo.SysStatisticalResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计Controller
 *
 * @author dataplatform
 * @date 2021-07-30
 */
@Api(value = "统计规则控制器", tags = {"统计规则管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/system/statistical")
public class SysStatisticalApiController extends BaseController {


    private final ISysStatisticalRulesService iSysStatisticalRulesService;

    /**
     * 导出统计结果列表
     */
    ////@ApiOperation("导出统计结果列表")
    
    @GetMapping("/export/{id}")
    public AjaxResult<SysStatisticalResultVo> export(@PathVariable Long id) {
        List<SysStatisticalResultVo> list = iSysStatisticalRulesService.queryList(id);
        SysStatisticalRules iSysStatisticalRulesServiceById = iSysStatisticalRulesService.getById(id);
        ExcelUtil<SysStatisticalResultVo> util = new ExcelUtil<SysStatisticalResultVo>(SysStatisticalResultVo.class);
        LinkedHashMap<String, String> listTitle = new LinkedHashMap<String, String>() {{
            put("统计规则名称", iSysStatisticalRulesServiceById.getJobName());
            put("统计周期", StatisticsPeriodType.findType(iSysStatisticalRulesServiceById.getStatisticsPeriodType()).getDescribe() +"  "+ iSysStatisticalRulesServiceById.getStatisticsTime());
            put("创建人", iSysStatisticalRulesServiceById.getCreateBy());
        }};
        util.setListTitle(listTitle);
        return util.exportExcel(list, "统计结果", iSysStatisticalRulesServiceById.getJobName() + "_" + DateUtils.dateTimeNow() + ".xlsx");
    }


    //@ApiOperation(value = "查询要统计规则", response = StatisticalInfoDto.class)
    
    @GetMapping("/getStatistical/{id}")
    public AjaxResult getStatistical(@PathVariable Long id) {
        return AjaxResult.success(iSysStatisticalRulesService.getStatistical(id));
    }

    //@ApiOperation(value = "查询要统计规则列表", response = StatisticalInfoDto.class)
    @GetMapping("/getStatisticalList")
    public TableDataInfo<StatisticalInfoDto> getStatisticalList() {
        startPage();
        List<StatisticalInfoDto> statisticalList = iSysStatisticalRulesService.getStatisticalList();
        return getDataTable(statisticalList);
    }

    /**
     * 获取统计周期
     */
    //@ApiOperation("获取统计周期类型")
    
    @GetMapping("/getStatisticsPeriodType")
    public AjaxResult<Map<String, String>> getStatisticsPeriodType() {
        return AjaxResult.success(iSysStatisticalRulesService.getStatisticsPeriodType());
    }

    /**
     * 获取统计内容类型
     */
    //@ApiOperation("获取统计内容类型")
    
    @GetMapping("/getStatisticsType")
    public AjaxResult<Map<String, String>> getStatisticsType() {
        return AjaxResult.success(iSysStatisticalRulesService.getStatisticsType());
    }

    /**
     * 新增统计规则
     */
    //@ApiOperation("新增或修改统计规则")
    
    @Log(title = "统计规则", businessType = BusinessType.INSERT)
    @PostMapping("/addOrUpdate")
    public AjaxResult<Object> addOrUpdate(@ApiParam @RequestBody SysStatisticalRulesDto bo) {
        return toAjax(iSysStatisticalRulesService.addOrUpdate(bo) ? 1 : 0);
    }

    /**
     * 删除统计规则
     */
    //@ApiOperation("删除统计规则")
    
    @Log(title = "统计规则", businessType = BusinessType.DELETE)
    @DeleteMapping("delete/{ids}")
    public AjaxResult<Object> remove(@PathVariable Long[] ids) {
        return toAjax(iSysStatisticalRulesService.removeByIds(Arrays.asList(ids)) ? 1 : 0);
    }

    /**
     * 暂停统计规则
     */
    //@ApiOperation("关闭统计规则")
    
    @Log(title = "统计规则", businessType = BusinessType.UPDATE)
    @GetMapping("suspend/{ids}")
    public AjaxResult<Object> suspend(@PathVariable Long ids) {
        return toAjax(iSysStatisticalRulesService.statusWithValidByIdsJurisdiction(ids, "1") ? 1 : 0);
    }

    /**
     * 暂停统计规则
     */
    //@ApiOperation("开启统计规则")
    
    @Log(title = "统计规则", businessType = BusinessType.UPDATE)
    @GetMapping("open/{ids}")
    public AjaxResult<Object> open(@PathVariable Long ids) {
        return toAjax(iSysStatisticalRulesService.statusWithValidByIdsJurisdiction(ids, "0") ? 1 : 0);
    }

    /**
     * 新增统计规则表
     */
    //@ApiOperation("新增要统计的表")
    
    @PostMapping("/addTable")
    public AjaxResult<Object> addTable(@ApiParam @RequestBody SysStatisticalTableDto bo) {
        return toAjax(iSysStatisticalRulesService.addTable(bo) ? 1 : 0);
    }


    //@ApiOperation(value = "查询要统计的表", response = TableInfoDto.class)
    
    @PostMapping("/getTableList")
    public AjaxResult getTableList(@RequestBody List<Long> ids) {
        return AjaxResult.success(iSysStatisticalRulesService.getTableList(ids));
    }


    //@ApiOperation("手动统计接口")
    
    @GetMapping("/statisticsTask/{id}")
    public AjaxResult<Object> statisticsTask(@PathVariable Long id) {
        return toAjax(iSysStatisticalRulesService.statisticsTask(id, "2") ? 1 : 0);
    }


}
