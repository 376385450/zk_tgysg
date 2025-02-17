package com.sinohealth.system.biz.dataassets.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchInfoDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowAutoBatchCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchEditRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AutoFlowBatchPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsAutoPageRequest;
import com.sinohealth.system.biz.dataassets.vo.AssetsFlowBatchVO;

import java.util.List;
import java.util.Map;

/**
 * @author Kuangcp
 * 2024-07-16 14:48
 */
public interface AssetsFlowService {

    AjaxResult<IPage<AssetsFlowBatchPageDTO>> pageQueryBatch(AssetsFlowBatchPageRequest request);

    /**
     * 创建及未创建 资产明细查询
     * <p>
     * 不能做分页，定制逻辑很多
     */
    AjaxResult<List<FlowAssetsPageDTO>> listAssets(FlowAssetsAutoPageRequest request);

    AjaxResult<Void> createBatch(AssetsFlowBatchCreateRequest request);

    AjaxResult<Void> deleteBatch(Long batchId);

    AjaxResult<Void> editBatch(AssetsFlowBatchEditRequest request);

    AjaxResult<AssetsFlowBatchInfoDTO> batchDetail(Long id);

    AjaxResult<Void> upsertAutoBatch(AssetsFlowAutoBatchCreateRequest request);

    AjaxResult<Void> deleteAutoBatch(Long id);

    List<AssetsFlowAutoBatchPageDTO> listAutoBatch(AutoFlowBatchPageRequest request);

    AjaxResult<List<AssetsFlowAutoBatchPageDTO>> listAutoBatchByScheduler(AutoFlowBatchPageRequest request);

    AjaxResult<AssetsFlowAutoBatchDTO> queryAutoBatch(Long id);

    /**
     * 根据业务查询对应要素
     *
     * @param bizIds 关联编号id
     * @return 工作流信息
     */
    List<AssetsFlowBatchVO> queryByBizIds(List<Long> bizIds);

    void autoCreateBatch(Long autoId);

    List<FlowAssetsPageDTO> queryAllScheduleAssets();

    Map<String, String> queryApplyFormScheduler();
}
