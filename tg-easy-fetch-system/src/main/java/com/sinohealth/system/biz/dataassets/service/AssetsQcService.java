package com.sinohealth.system.biz.dataassets.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.constant.AssetsQcTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcDetail;
import com.sinohealth.system.biz.dataassets.dto.AssetsQcPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsQcPageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn 2024-06-17 16:24
 */
public interface AssetsQcService {

    AjaxResult<IPage<AssetsQcPageDTO>> pageQuery(AssetsQcPageRequest request);

    /**
     * 创建所有开启开关的全部资产的推送任务
     */
    AjaxResult<Void> createAllQc(Long bizId);

    Optional<Long> queryRunBatch();

    /**
     * 已有批次则直接使用，否则创建空批次
     */
    Optional<Long> createOrFillBatch(AssetsQcTypeEnum actType, List<AssetsQcDetail> details, Long bizId);

    List<AssetsQcDetail> buildWideDetail(Long tableId);

    List<AssetsQcDetail> buildDetailsByAssets(Collection<Long> assetsIds, AssetsQcTypeEnum qcType, Long bizId);

    List<AssetsQcDetail> buildDetailsByApply(Collection<Long> applyIds, AssetsQcTypeEnum qcType);

    // List<AssetsQcDetail> queryFlowDetail(Collection<Long> assetsIds);
    //
    // void startWideUpgrade(Long batchId, List<AssetsQcDetail> details);

    /**
     * 宽表资产升级 触发完成
     */
    void finishWideUpgrade(Long batchId);

    /**
     * 工作流资产升级 触发完成
     */
    void finishFlowUpgrade(Long batchId);

    void dolphinCallBack(String instanceUid, Integer state);

    /**
     * 根据业务关联编号，获取qc详细信息
     *
     * @param bizIds 业务编号
     * @return qc详细信息
     */
    List<AssetsQcDetail> queryByBizIds(List<Long> bizIds);

    /**
     * 根据业务关联编号，获取qc批次信息
     *
     * @param bizIds 业务编号
     * @return qc批次信息
     */
    List<AssetsQcBatch> queryBatchByBizIds(List<Long> bizIds);
}
