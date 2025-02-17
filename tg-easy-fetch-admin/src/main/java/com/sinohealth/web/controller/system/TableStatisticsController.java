package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.system.domain.TableStatistics;
import com.sinohealth.system.service.ITableStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Controller
 *
 * @author dataplatform
 * @date 2021-05-07
 */
@Api( tags = {"使用监控"})
@RestController
@RequestMapping("/system/statistics")
public class TableStatisticsController extends BaseController {

    @Autowired
    private ITableStatisticsService tableStatisticsService;


    //@ApiOperation(value = "列表", response = TableStatistics.class)
    @GetMapping("/list")
    public TableDataInfo<TableStatistics> list(@RequestParam Long tableId) {
        startPage(" id desc");
        return getDataTable(tableStatisticsService.list(Wrappers.<TableStatistics>query().eq("table_id", tableId)));
    }

    //@ApiOperation(value = "曲线图", response = TableStatistics.class)
    @GetMapping("/map")
    public TableDataInfo<TableStatistics> map(@RequestParam Long tableId, @RequestParam Date startDate, @RequestParam Date endDate) {
        List<TableStatistics> list=tableStatisticsService.list(Wrappers.<TableStatistics>query().eq("table_id", tableId).gt("create_time", DateUtils.getStartTime(startDate,-1)).lt("create_time", DateUtils.getEndTime(endDate,1)).orderByDesc("id"));
        list.sort(Comparator.comparing(TableStatistics::getId));
        return getDataTable(list);
    }


}
