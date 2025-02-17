package com.sinohealth.system.biz.scheduler.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.scheduler.dto.UpsertSchedulerTaskBO;
import com.sinohealth.system.biz.scheduler.dto.UpsertTaskVO;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-02 16:41
 */
public interface IntegrateSyncTaskService {

    /**
     * 创建同步配置，创建关联的工作流
     */
    AjaxResult<UpsertTaskVO> upsertTask(UpsertSchedulerTaskBO bo);

    AjaxResult executeWorkFlow(Long applicationId);

    /**
     * 异步下架工作流 调用尚书台
     *
     * @param assetId 资产id
     */
    void asyncOfflineWorkFlow(Long assetId);

    AjaxResult<Void> offlineExpireFlow();

    /**
     * 创建或更新 尚书台 同步配置，工作流
     */
    AjaxResult<Void> upsertTaskConfigAndProcess(Long applyId);

}
