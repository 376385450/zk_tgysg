package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 17:30
 */
public interface AssetsWideUpgradeTriggerDAO extends IService<AssetsWideUpgradeTrigger> {

    boolean queryNeedTableIds(Long tableId, String endTime);
}
