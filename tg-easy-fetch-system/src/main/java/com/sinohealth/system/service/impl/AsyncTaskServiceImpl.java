package com.sinohealth.system.service.impl;/**
 * @author linshiye
 */

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.system.domain.AsyncTask;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.dto.system.AsyncTaskPageRequest;
import com.sinohealth.system.mapper.AsyncTaskMapper;
import com.sinohealth.system.service.IAsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-03-09 4:51 下午
 */
@Slf4j
@Service
public class AsyncTaskServiceImpl extends ServiceImpl<AsyncTaskMapper, AsyncTask> implements IAsyncTaskService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Boolean addAsyncTask(AsyncTaskDto asyncTaskDto) {
        Long id = null;
        try {
            AsyncTask asyncTask = new AsyncTask();
            BeanUtils.copyProperties(asyncTaskDto, asyncTask);
            asyncTask.setTime(new Date());
            asyncTask.setUserId(asyncTaskDto.getUserId());
            asyncTask.setStatus(AsyncTaskConst.Status.HANGING);
            asyncTask.setFlowStatus(AsyncTaskConst.FLOW_STATUS.CREATED);
            baseMapper.insert(asyncTask);

            id = asyncTask.getId();
            redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, id);
        } catch (Exception e) {
            if (Objects.nonNull(id)) {
                redisTemplate.opsForList().remove(RedisKeys.TASK_QUEUE, 1, id);
            }
            throw e;
        }

        return true;
    }

    @Override
    public AjaxResult retryAsyncTask(Long taskId) {
        log.info("retry taskId={}", taskId);
        if (Objects.nonNull(taskId)) {
            redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, taskId);
        }
        AsyncTask asyncTask = this.getById(taskId);
        if (asyncTask == null) {
            log.warn("not exist: id={}", taskId);
            return AjaxResult.error("任务不存在");
        }

        if (!Objects.equals(AsyncTaskConst.Status.FAILED, asyncTask.getStatus())){
            return AjaxResult.error("仅失败的任务可重试");
        }
        asyncTask.setUpdateTime(new Date());
        asyncTask.setStatus(AsyncTaskConst.Status.HANGING);
            asyncTask.setReadFlag(AsyncTaskConst.ReadFlag.READ);
        this.updateById(asyncTask);
        return AjaxResult.success();

    }

    @Override
    public Boolean updateAsyncTask(Long id, Integer status, String url) {
        if (id == null) {
            return false;
        }
        AsyncTask asyncTask = getBaseMapper().selectById(id);
        if (asyncTask == null) {
            return false;
        }
        asyncTask.setStatus(status);
        asyncTask.setUrl(url);
        asyncTask.setUpdateTime(new Date());
        updateById(asyncTask);
        return true;
    }


    @Override
    public List<AsyncTask> selectList(Integer status, Long userId, Integer flowStatus) {
        QueryWrapper<AsyncTask> queryWrapper = new QueryWrapper<>();
        Set<Integer> statusSet = new HashSet<>();
        queryWrapper.eq("del_flag", AsyncTaskConst.DEL_FLAG.NORMAL);
        if (status != null) {
            statusSet.add(status);
            if (status == AsyncTaskConst.Status.SUCCEED) {
                statusSet.add(AsyncTaskConst.Status.SUCCEED);
                statusSet.add(AsyncTaskConst.Status.FAILED);
            }
        }
        if (CollectionUtil.isNotEmpty(statusSet)) {
            queryWrapper.in("status", statusSet);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (flowStatus != null) {
            queryWrapper.eq("flow_status", flowStatus);
        }
        queryWrapper.orderByDesc("time");
        List<AsyncTask> asyncTasks = baseMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(asyncTasks)) {
            return new ArrayList<>();
        }
        return asyncTasks;
    }

    @Override
    public IPage<AsyncTask> pageList(AsyncTaskPageRequest pageRequest) {
        LambdaQueryWrapper<AsyncTask> queryWrapper = new QueryWrapper<AsyncTask>().lambda();
        Set<Integer> statusSet = new HashSet<>();
        queryWrapper.eq(AsyncTask::getDelFlag, AsyncTaskConst.DEL_FLAG.NORMAL);
        Integer status = pageRequest.getStatus();
        if (status != null) {
            statusSet.add(status);
            if (status == AsyncTaskConst.Status.HANGING) {
                statusSet.add(AsyncTaskConst.Status.FAILED);
            }
        }

        queryWrapper.in(CollectionUtil.isNotEmpty(statusSet), AsyncTask::getStatus, statusSet)
                .eq(Objects.nonNull(pageRequest.getUserId()), AsyncTask::getUserId, pageRequest.getUserId())
                .eq(Objects.nonNull(pageRequest.getFlowStatus()), AsyncTask::getFlowStatus, pageRequest.getFlowStatus())
                .orderByDesc(AsyncTask::getTime);

        return baseMapper.selectPage(pageRequest.buildPage(), queryWrapper);
    }

    @Override
    public Integer countUnRead(AsyncTaskPageRequest pageRequest) {
        LambdaQueryWrapper<AsyncTask> queryWrapper = new QueryWrapper<AsyncTask>().lambda();
        Set<Integer> statusSet = new HashSet<>();
        queryWrapper.eq(AsyncTask::getDelFlag, AsyncTaskConst.DEL_FLAG.NORMAL);
        Integer status = pageRequest.getStatus();
        if (status != null) {
            statusSet.add(status);
            if (status == AsyncTaskConst.Status.HANGING) {
                statusSet.add(AsyncTaskConst.Status.FAILED);
            }
        }

        queryWrapper.in(CollectionUtil.isNotEmpty(statusSet), AsyncTask::getStatus, statusSet)
                .eq(Objects.nonNull(pageRequest.getUserId()), AsyncTask::getUserId, pageRequest.getUserId())
                .eq(Objects.nonNull(pageRequest.getFlowStatus()), AsyncTask::getFlowStatus, pageRequest.getFlowStatus())
                .eq(AsyncTask::getReadFlag, AsyncTaskConst.ReadFlag.NO_READ)
        ;

        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public Integer countList(Integer flowStatus) {
        QueryWrapper<AsyncTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", AsyncTaskConst.DEL_FLAG.NORMAL);
        if (flowStatus != null) {
            queryWrapper.eq("flow_status", flowStatus);
        }
        queryWrapper.orderByDesc("time");
        return baseMapper.selectCount(queryWrapper);
    }
}
