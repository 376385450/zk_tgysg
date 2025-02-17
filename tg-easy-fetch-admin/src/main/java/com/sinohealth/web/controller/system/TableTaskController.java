package com.sinohealth.web.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.core.page.TableSupport;
import com.sinohealth.common.enums.SpeedOfProgressType;
import com.sinohealth.common.enums.TaskType;
import com.sinohealth.common.enums.TimeType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.TableTask;
import com.sinohealth.system.dto.TableTaskDto;
import com.sinohealth.system.dto.query.TableTaskVo;
import com.sinohealth.system.service.ITableTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"数据地图任务列表接口"})
@RestController
@RequestMapping("/system/table-task")
public class TableTaskController extends BaseController {

    @Autowired
    ITableTaskService iTableTaskService;

// @PreAuthorize("@ss.hasPermi('system:table-task:list')")
    //@ApiOperation(value = "列表", response = TableTask.class)
    @GetMapping("/list")
    public AjaxResult<IPage> list(TableTaskVo tableTaskVo) {
        LambdaQueryWrapper<TableTask> lqw = new LambdaQueryWrapper();
        if (!StringUtils.isEmpty(tableTaskVo.getOperator())) {
            lqw.like(TableTask::getOperator, tableTaskVo.getOperator());
        }
        if (null != tableTaskVo.getTaskType()) {
            lqw.eq(TableTask::getTaskType, tableTaskVo.getTaskType());
        }
        if (null != tableTaskVo.getSpeedOfProgress()) {
            lqw.eq(TableTask::getSpeedOfProgress, tableTaskVo.getSpeedOfProgress());
        }
        if (null != tableTaskVo.getStartTime() && null != tableTaskVo.getEndTime()) {
            lqw.and(lq->lq.ge(TableTask::getCreateTime, tableTaskVo.getStartTime()).le(TableTask::getCreateTime, tableTaskVo.getEndTime()));
        }
        if(!SecurityUtils.getLoginUser().isAdmin()){
            lqw.eq(TableTask::getOperatorId, SecurityUtils.getUserId());
        }
        lqw.orderByDesc(TableTask::getCreateTime);
        IPage<TableTask> page = iTableTaskService.page(new Page<TableTask>(TableSupport.buildPageRequest().getPageNum(),TableSupport.buildPageRequest().getPageSize()),lqw);
        IPage pageData = page;
        pageData.setRecords(page.getRecords().parallelStream().map(i -> {
            TableTaskDto tableTaskDto = new TableTaskDto();
            BeanUtils.copyProperties(i, tableTaskDto);
            tableTaskDto.setTaskType(TaskType.getTaskType(i.getTaskType()));
            tableTaskDto.setSpeedOfProgress(SpeedOfProgressType.getSpeedOfProgressType(i.getSpeedOfProgress()));
            if(null != tableTaskDto.getCreateTime() && null != tableTaskDto.getCompleteTime()){
                tableTaskDto.setUseTime(DateUtils.getDatePoor(i.getCompleteTime(),i.getCreateTime(), TimeType.MINUTES,TimeType.SECONDS));
            }
            return tableTaskDto;
        }).collect(Collectors.toList()));
        return AjaxResult.success(pageData);
    }

}
