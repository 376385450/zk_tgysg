package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.AsyncTask;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.dto.system.AsyncTaskPageRequest;

import java.util.List;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-03-09 4:42 下午
 */
public interface IAsyncTaskService extends IService<AsyncTask> {

    /**
     * 添加异步任务
     */
    Boolean addAsyncTask(AsyncTaskDto asyncTaskDto);

    AjaxResult retryAsyncTask(Long taskId);

    Boolean updateAsyncTask(Long id, Integer status, String url);

    List<AsyncTask> selectList(Integer status, Long userId, Integer flowStatus);

    IPage<AsyncTask> pageList(AsyncTaskPageRequest pageRequest);

    Integer countUnRead(AsyncTaskPageRequest pageRequest);

    Integer countList(Integer flowStatus);
}
