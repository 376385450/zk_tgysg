package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:18
 */
public interface PowerBiPushDetailDAO extends IService<PowerBiPushDetail> {

    Map<Long, List<PowerBiPushDetail>> queryStateDetailByBatch(Collection<Long> batchIds);

    void updateStartState(Long id);

    void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum, String runLog);
}
