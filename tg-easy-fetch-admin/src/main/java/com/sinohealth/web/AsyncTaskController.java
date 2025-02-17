package com.sinohealth.web;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.AsyncTask;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.dto.system.AsyncTaskPageRequest;
import com.sinohealth.system.dto.system.AsyncTaskPageVO;
import com.sinohealth.system.dto.system.AsyncTaskVo;
import com.sinohealth.system.service.IAsyncTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-07 11:57 上午
 */
@Api(tags = {"异步任务中心"})
@RestController
@RequestMapping({"/system/async-task","/api/system/async-task"})
@Slf4j
public class AsyncTaskController extends BaseController {

    @Autowired
    private IAsyncTaskService asyncTaskService;

    /**
     * 查询列表
     */
    @ApiOperation("查询列表")
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态（0 已完成 1 进行中）", required = true, type = "int")
    })
    public AjaxResult<AsyncTaskPageVO> queryAsyncTaskList(@RequestParam(value = "status") Integer status,
                                                          @RequestParam(value = "page") Integer page,
                                                          @RequestParam(value = "size") Integer size) {
        Long userId = SecurityUtils.getUserId();
        AsyncTaskPageRequest request = AsyncTaskPageRequest.builder().status(status).userId(userId).build();
        request.setPage(page);
        request.setSize(size);
        IPage<AsyncTask> pageResult = asyncTaskService.pageList(request);

        List<AsyncTask> tasks = pageResult.getRecords();
        Page<AsyncTaskVo> newPageResult = new Page<>();
        newPageResult.setTotal(pageResult.getTotal());
        newPageResult.setCurrent(pageResult.getCurrent());
        newPageResult.setPages(pageResult.getPages());
        newPageResult.setSize(pageResult.getSize());
        newPageResult.setRecords(tasks.stream().map(v -> {
            AsyncTaskVo asyncTaskVo = new AsyncTaskVo();
            BeanUtils.copyProperties(v, asyncTaskVo);
            if (!Objects.equals(v.getBusinessType(), AsyncTaskConst.BUSINESS_TYPE.DELIVERY_TABLE)) {
                asyncTaskVo.setParamJson("");
            }
            return asyncTaskVo;
        }).collect(Collectors.toList()));

        Integer unRead = asyncTaskService.countUnRead(request);
        AsyncTaskPageVO pageVO = AsyncTaskPageVO.builder().pages(newPageResult).unRead(unRead).build();
        return AjaxResult.success(pageVO);
    }

    @GetMapping("/retry/{id}")
    public AjaxResult retry(@PathVariable("id") Long id) {
        return asyncTaskService.retryAsyncTask(id);
    }

    @GetMapping("/cancel/{id}")
    public AjaxResult<Object> cancel(@PathVariable("id") Long id) {
        AsyncTask asyncTask = asyncTaskService.getById(id);
        if (asyncTask == null) {
            log.warn("not exist: id={}", id);
            return AjaxResult.error("任务不存在");
        }

        if (!Objects.equals(AsyncTaskConst.Status.FAILED, asyncTask.getStatus())) {
            return AjaxResult.error("仅失败的任务可取消");
        }
        asyncTask.setUpdateTime(new Date());
        asyncTask.setReadFlag(AsyncTaskConst.ReadFlag.READ);
        asyncTask.setDelFlag(AsyncTaskConst.DEL_FLAG.DELETED);
        asyncTaskService.updateById(asyncTask);
        return AjaxResult.success();
    }

    @GetMapping("/deleteAllSuccessTask")
    public AjaxResult<Void> deleteAllSuccessTask() {
        asyncTaskService.getBaseMapper().update(null, new UpdateWrapper<AsyncTask>().lambda()
                .set(AsyncTask::getDelFlag, AsyncTaskConst.DEL_FLAG.DELETED)
                .eq(AsyncTask::getUserId, SecurityUtils.getUserId())
                .eq(AsyncTask::getDelFlag, AsyncTaskConst.DEL_FLAG.NORMAL)
                .eq(AsyncTask::getStatus, AsyncTaskConst.Status.SUCCEED)
        );
        return AjaxResult.succeed();
    }

    @GetMapping("/markRead/{id}")
    public AjaxResult<Object> markRead(@PathVariable("id") Long id) {
        AsyncTask asyncTask = asyncTaskService.getById(id);
        if (asyncTask == null) {
            log.warn("not exist: id={}", id);
            return AjaxResult.error();
        }
        asyncTask.setUpdateTime(new Date());
        asyncTask.setReadFlag(AsyncTaskConst.ReadFlag.READ);
        asyncTaskService.updateById(asyncTask);
        return AjaxResult.success();
    }

}
