package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcDetail;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:21
 */
public interface AssetsQcDetailDAO extends IService<AssetsQcDetail> {

    void updateStartState(Long id);

    void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum, String runLog);
}
