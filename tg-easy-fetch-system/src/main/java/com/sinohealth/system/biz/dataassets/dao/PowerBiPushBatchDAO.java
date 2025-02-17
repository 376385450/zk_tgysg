package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:21
 */
public interface PowerBiPushBatchDAO extends IService<PowerBiPushBatch> {

    void updateState(Long id, AssetsUpgradeStateEnum stateEnum);

    boolean updateState(Long id, LocalDateTime finishTime, AssetsUpgradeStateEnum stateEnum);

    void updateState(Collection<Long> id, AssetsUpgradeStateEnum stateEnum);

    void logicDelete(Long id);
}
