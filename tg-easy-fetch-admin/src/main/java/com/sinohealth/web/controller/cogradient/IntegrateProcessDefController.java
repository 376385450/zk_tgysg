///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.sinohealth.web.controller.cogradient;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.sinohealth.common.config.AppProperties;
//import com.sinohealth.common.constant.DsConstants;
//import com.sinohealth.common.core.controller.BaseController;
//import com.sinohealth.common.core.domain.AjaxResult;
//import com.sinohealth.common.enums.*;
//import com.sinohealth.common.utils.StringUtils;
//import com.sinohealth.system.dto.TgCogradientDetailDto;
//import com.sinohealth.system.dto.TgCogradientInfoDto;
//import com.sinohealth.system.dto.TgCogradientMonitorDto;
//import com.sinohealth.system.service.IntergrateProcessDefService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Objects;
//import java.util.function.Supplier;
//
//
///**
// * 数据集成-工作流定义
// *
// * @author penghaiqiu
// * @date 2022/05/23
// * @since 1.6.2
// */
//@Slf4j
//@Api(tags = "同步任务")
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@RestController
//@RequestMapping("/integrate/process")
//public class IntegrateProcessDefController extends BaseController {
//
//    @Autowired
//    private IntergrateProcessDefService intergrateProcessDefService;
//    @Autowired
//    private AppProperties appProperties;
//
//    @ApiOperation(value = "保存任务")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "name", value = "流程定义名称", required = true, type = "String"),
//            @ApiImplicitParam(name = "processDefinitionJson", value = "流程定义详细信息(json格式)", required = true, type = "String"),
//            @ApiImplicitParam(name = "locations", value = "流程定义节点坐标位置信息(json格式)", required = true, type = "String"),
//            @ApiImplicitParam(name = "connects", value = "流程定义节点图标连接信息(json格式)", required = true, type = "String"),
//            @ApiImplicitParam(name = "id", value = "任务id", required = false, type = "int"),
//            @ApiImplicitParam(name = "tableId", value = "同步表Id", required = true, type = "int"),
//            @ApiImplicitParam(name = "crontab", value = "调度频率", required = false, type = "String"),
//            @ApiImplicitParam(name = "releaseState", value = "上下线状态", required = false, type = "int"),
//    })
//    @PostMapping(value = "/save")
//    @ResponseStatus(HttpStatus.CREATED)
//    public AjaxResult createProcessDefinition(
//            @RequestParam(value = "name", required = true) String name,
//            @RequestParam(value = "id", required = false) Long id,
//            @RequestParam(value = "tableId", required = false) Long tableId,
//            @RequestParam(value = "processDefinitionJson", required = true) String json,
//            @RequestParam(value = "locations", required = true) String locations,
//            @RequestParam(value = "connects", required = true) String connects,
//            @RequestParam(value = "crontab", required = false) String crontab,
//            @RequestParam(value = "releaseState", required = true) int releaseState) {
//
//
//        return wrapException(() -> intergrateProcessDefService.createProcessDefinition(id, tableId, name, json, crontab, locations, connects, releaseState, processId -> {
//        }));
//    }
//
//
//    @ApiOperation(value = "上下线")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "任务id", required = true, dataType = "Int", example = "1"),
//            @ApiImplicitParam(name = "releaseState", value = "流程状态0:下线,1:上线'", required = true, dataType = "Int", example = "0"),
//    })
//    @PostMapping(value = "/release")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult releaseProcessDefinition(
//            @RequestParam(value = "id", required = true) Integer id,
//            @RequestParam(value = "releaseState", required = true) Integer releaseState) {
//
//        return wrapException(() -> intergrateProcessDefService.release(id, releaseState));
//    }
//
//    @ApiOperation(value = "任务列表", response = TgCogradientInfoDto.class)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "searchVal", value = "搜索值", dataType = "String"),
//            @ApiImplicitParam(name = "pageNo", value = "页码号", dataType = "Int", example = "1"),
//            @ApiImplicitParam(name = "pageSize", value = "页大小", dataType = "Int", example = "20")
//    })
//    @GetMapping(value = "/list-paging")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult queryDataSourceListPaging(
//            @RequestParam(value = "searchVal", required = false) String searchVal,
//            @RequestParam("pageNo") Integer pageNo,
//            @RequestParam("pageSize") Integer pageSize) {
//        final Page<TgCogradientInfoDto> page = new Page<>(pageNo, pageSize);
//        return wrapException(() -> AjaxResult.success(intergrateProcessDefService.queryListPaging(page, searchVal)));
//    }
//
//
//    @ApiOperation(value = "运行任务")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "processDefinitionId", value = "流程定义ID", required = true, dataType = "Int", example = "100"),
//            @ApiImplicitParam(name = "failureStrategy", value = "失败策略,可用值:END,CONTINUE", required = true, dataType = "FailureStrategy"),
//            @ApiImplicitParam(name = "taskDependType", value = "任务依赖类型,可用值:TASK_ONLY,TASK_PRE,TASK_POST", dataType = "TaskDependType"),
//            @ApiImplicitParam(name = "execType", value = "指令类型,可用值:START_PROCESS,START_CURRENT_TASK_PROCESS,RECOVER_TOLERANCE_FAULT_PROCESS,RECOVER_SUSPENDED_PROCESS,START_FAILURE_TASK_PROCESS,COMPLEMENT_DATA,SCHEDULER,REPEAT_RUNNING,PAUSE,STOP,RECOVER_WAITTING_THREAD", dataType = "CommandType"),
//            @ApiImplicitParam(name = "warningType", value = "发送策略,可用值:NONE,SUCCESS,FAILURE,ALL", required = true, dataType = "WarningType"),
//            @ApiImplicitParam(name = "warningGroupId", value = "发送组ID", required = true, dataType = "Int", example = "100"),
//            @ApiImplicitParam(name = "runMode", value = "运行模式,可用值:RUN_MODE_SERIAL,RUN_MODE_PARALLEL", dataType = "RunMode"),
//            @ApiImplicitParam(name = "processInstancePriority", value = "流程实例优先级,可用值:HIGHEST,HIGH,MEDIUM,LOW,LOWEST", required = true, dataType = "Priority"),
//            @ApiImplicitParam(name = "workerGroup", value = "Worker分组", dataType = "String", example = "default"),
//            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int", example = "100"),
//    })
//    @PostMapping(value = "start-process-instance")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult startProcessInstance(
//            @RequestParam(value = "processDefinitionId") int processDefinitionId,
//            @RequestParam(value = "failureStrategy", required = true) FailureStrategy failureStrategy,
//            @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
//            @RequestParam(value = "execType", required = false) CommandType execType,
//            @RequestParam(value = "warningType", required = true) WarningType warningType,
//            @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
//            @RequestParam(value = "runMode", required = false) RunMode runMode,
//            @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
//            @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
//            @RequestParam(value = "timeout", required = false) Integer timeout,
//            @RequestParam(value = "startParams", required = false) String startParams) {
//        if (timeout == null) {
//            timeout = DsConstants.MAX_TASK_TIMEOUT;
//        }
//        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
//        postParameters.add("processDefinitionId", processDefinitionId);
//        postParameters.add("failureStrategy", failureStrategy);
//        postParameters.add("taskDependType", taskDependType);
//        postParameters.add("execType", execType);
//        postParameters.add("warningType", StringUtils.isNotEmpty(appProperties.getWarningType()) ? appProperties.getWarningType() : warningType);
//        postParameters.add("warningGroupId", Objects.nonNull(appProperties.getWarningGroupId()) ? appProperties.getWarningGroupId() : warningGroupId);
//        postParameters.add("runMode", runMode);
//        postParameters.add("processInstancePriority", processInstancePriority);
//        postParameters.add("workerGroup", workerGroup);
//        postParameters.add("timeout", timeout);
//        return wrapException(() -> intergrateProcessDefService.execProcessInstance(postParameters));
//
//    }
//
//    @ApiOperation(value = "删除")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "任务id", dataType = "Int", example = "100")
//    })
//    @GetMapping(value = "/delete")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult deleteProcessDefinitionById(@RequestParam("id") Integer id) {
//        return wrapException(() -> intergrateProcessDefService.delete(id));
//    }
//
//    @ApiOperation(value = "同步详情", response = TgCogradientDetailDto.class)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "任务id", dataType = "int"),
//            @ApiImplicitParam(name = "tableId", value = "表id", dataType = "int"),
//            @ApiImplicitParam(name = "state", value = "同步状态", dataType = "int"),
//            @ApiImplicitParam(name = "pageNo", value = "页码号", dataType = "Int", example = "1"),
//            @ApiImplicitParam(name = "pageSize", value = "页大小", dataType = "Int", example = "20")
//    })
//    @GetMapping(value = "/syncDetail")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult syncDetail(
//            @RequestParam(value = "id", required = false) Integer id,
//            @RequestParam(value = "tableId", required = false) Integer tableId,
//            @RequestParam(value = "state", required = false) Integer state,
//            @RequestParam("pageNo") Integer pageNo,
//            @RequestParam("pageSize") Integer pageSize) {
//        return wrapException(() -> AjaxResult.success(intergrateProcessDefService.querySyncDetail(id, tableId, state, pageNo, pageSize)));
//    }
//
//
//    @ApiOperation(value = "同步监控", response = TgCogradientMonitorDto.class)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String"),
//            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String")
//    })
//    @GetMapping(value = "/monitorDetail")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult monitorDetail(@RequestParam(value = "startTime", required = false) String startTime,
//                                    @RequestParam(value = "endTime", required = false) String endTime) {
//        return wrapException(() -> intergrateProcessDefService.countTaskState(startTime, endTime));
//    }
//
//    @ApiOperation(value = "查询任务实例日志")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", dataType = "Int", example = "100"),
//            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", dataType = "Int", example = "100"),
//            @ApiImplicitParam(name = "limit", value = "LIMIT", dataType = "Int", example = "100")
//    })
//    @GetMapping(value = "/detail")
//    @ResponseStatus(HttpStatus.OK)
//    public AjaxResult queryLog(
//            @RequestParam(value = "taskInstanceId") int taskInstanceId,
//            @RequestParam(value = "skipLineNum") int skipNum,
//            @RequestParam(value = "limit") int limit) {
//
//        return wrapException(() -> intergrateProcessDefService.queryLog(taskInstanceId, skipNum, limit));
//    }
//
//
//    @ApiOperation(value = "下载任务实例日志")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", dataType = "Int", example = "100")
//    })
//    @GetMapping(value = "/download-log")
//    @ResponseBody
//    public ResponseEntity downloadTaskLog(@RequestParam(value = "taskInstanceId") int taskInstanceId) {
//        return intergrateProcessDefService.getLogBytes(taskInstanceId);
//    }
//
//
//    @GetMapping(value = "/page")
//    @ApiOperation("分页查询流程列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "pageNo", example = "1,2,3,4"),
//            @ApiImplicitParam(name = "pageSize", example = "10,20,99999"),
//            @ApiImplicitParam(name = "searchVal", required = false, example = "参照尚书台的流程搜索字"),
//            @ApiImplicitParam(name = "releaseState", required = false, example = "0下线的 1上线的")
//    })
//    public AjaxResult queryProcessDefinitionList(
//            @RequestParam(value = "pageNo") Integer pageNo,
//            @RequestParam(value = "pageSize") Integer pageSize,
//            @RequestParam(value = "searchVal", required = false) String searchVal,
//            @RequestParam(value = "releaseState", required = false) Integer releaseState) {
//        return wrapException(() -> intergrateProcessDefService.queryProcessDefinitionList(pageNo, pageSize, searchVal, releaseState));
//    }
//
//    @GetMapping(value = "/queryById")
//    @ApiOperation("分页查询流程列表")
//    public AjaxResult queryProcessDefinitionById(@RequestParam(value = "processId") Integer processId) {
//        return wrapException(() -> intergrateProcessDefService.queryProcessById(processId));
//    }
//
//    private AjaxResult wrapException(Supplier<AjaxResult> func) {
//        try {
//            return func.get();
//        } catch (Exception e) {
//            log.error("", e);
//            return AjaxResult.error("查询尚书台异常");
//        }
//    }
//
//}
