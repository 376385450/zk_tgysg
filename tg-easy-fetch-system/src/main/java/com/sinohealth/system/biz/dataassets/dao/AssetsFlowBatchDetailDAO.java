package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-07-17 10:19
 */
public interface AssetsFlowBatchDetailDAO extends IService<AssetsFlowBatchDetail> {

    Optional<Long> queryBatchId(Long detailId);

    List<AssetsFlowBatchDetail> queryByBatchId(Long batchId);

    List<AssetsFlowBatchDetail> queryByBatchId(Collection<Long> batchIds);

    void updateState(Long applyId, AssetsUpgradeStateEnum state);

    void updateState(Collection<Long> applyIds, AssetsUpgradeStateEnum state);
}
