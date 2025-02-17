package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;

import java.util.Collection;

/**
 * @author Kuangcp
 * 2024-07-16 21:29
 */
public interface AssetsFlowBatchDAO extends IService<AssetsFlowBatch> {


    boolean updateState(Long batchId, AssetsUpgradeStateEnum state);

    void updateState(Collection<Long> batchIds, AssetsUpgradeStateEnum state);
}
