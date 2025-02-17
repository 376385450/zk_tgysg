package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.dto.QueryTableCountDto;
import com.sinohealth.system.dto.TableLogMapDto;
import com.sinohealth.system.dto.TableLogUpdateDto;
import com.sinohealth.system.dto.UserQueryTableLogDto;
import com.sinohealth.system.service.ITableLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 变更记录Controller
 *
 * @author jingjun
 * @date 2021-04-16
 */
@RestController
@RequestMapping("/system/log")
@Api(tags = {"变更记录"})
public class TableLogController extends BaseController {
    @Autowired
    private ITableLogService tableLogService;

    @GetMapping("/list")
    @ApiOperation(value = "列表", response = TableLog.class)
    public TableDataInfo list(@RequestParam(required = false) Long dirId, @RequestParam(required = false) Long tableId, @RequestParam(required = false) Integer logType, @RequestParam(required = false) Long userId, @ApiParam(value = "变更类型： 元数据变更 metadata , 表数据变更 data") @RequestParam(required = false) String changeType) {
        startPage(" id desc");
        List<TableLog> list = tableLogService.getList(dirId == null ? null : Arrays.asList(dirId), tableId, logType, userId, changeType);
        return getDataTable(list);
    }

    @GetMapping("/queryTable")
    //@ApiOperation(value = "使用监控-最近使用用户(查询，导出)", response = UserQueryTableLogDto.class)
    public AjaxResult queryHistory() {
        return AjaxResult.success(tableLogService.getQueryAndExportTableLog(null));
    }

    @GetMapping("/queryTableMap")
    //@ApiOperation(value = "使用监控-使用次数监控表", response = QueryTableCountDto.class)
    public AjaxResult queryTableMap(@RequestParam(required = false) Long dirId, @RequestParam(required = false) String tableName, @RequestParam(required = false) Date startTime, @RequestParam(required = false) Date endTime) {
        return AjaxResult.success(tableLogService.queryTableMap(dirId, tableName, startTime, endTime));
    }

    @GetMapping("/week")
    //@ApiOperation(value = "我的概览-柱状图", response = TableLogMapDto.class)
    public AjaxResult queryTableMap() {
        return AjaxResult.success(tableLogService.getLast7DayLogStatistic(SecurityUtils.getUserId()));
    }

    @PutMapping("/{logId}")
    //@ApiOperation(value = "添加备注")
    public AjaxResult update(@ApiParam(hidden = true) @PathVariable("logId") Long logId, @Validated @RequestBody TableLogUpdateDto dto) {
        tableLogService.update(Wrappers.<TableLog>update().eq("id", logId).set("comment", dto.getComment()));
        return AjaxResult.success();
    }

    @GetMapping("/map")
    //@ApiOperation(value = "列表", response = TableLog.class)
    public AjaxResult map(@RequestParam Long tableId, @RequestParam Date startDate, @RequestParam Date endDate) {

        return AjaxResult.success(tableLogService.getOneTableMap(tableId, DateUtils.getEndTime(startDate, 0), DateUtils.getStartTime(endDate, 0)));
    }

}
