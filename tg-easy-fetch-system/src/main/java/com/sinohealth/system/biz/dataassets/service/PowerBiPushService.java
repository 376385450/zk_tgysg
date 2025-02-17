package com.sinohealth.system.biz.dataassets.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.PowerBiPushBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushPageRequest;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn 2024-05-25 09:54
 */
public interface PowerBiPushService {

    AjaxResult<Void> createPush(PowerBiPushCreateRequest request);

    /**
     * 重试一批中失败的推送
     */
    AjaxResult<Void> retryPush(Long pushId);

    /**
     * 重放推送
     */
    AjaxResult<Void> replayPush(Long pushId);

    AjaxResult<IPage<PowerBiPushBatchPageDTO>> pageQuery(PowerBiPushPageRequest request);

    AjaxResult<List<FlowAssetsPageDTO>> pageQueryAssets(FlowAssetsPageRequest request);

    AjaxResult<Void> delete(Long pushId);

    AjaxResult<String> queryLog(Long pushId);

    /**
     * 根据业务关联编号，查询powerBi任务信息
     * 
     * @param bizIds 业务该你了编号
     * @return powerBi信息
     */
    List<PowerBiPushBatch> queryByBizIds(List<Long> bizIds);
}
