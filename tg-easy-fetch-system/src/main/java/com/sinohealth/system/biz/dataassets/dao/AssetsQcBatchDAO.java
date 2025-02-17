package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;

import java.util.Collection;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:21
 */
public interface AssetsQcBatchDAO extends IService<AssetsQcBatch> {

    void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum);

    void updateState(Collection<Long> id, AssetsUpgradeStateEnum stateEnum);
}
