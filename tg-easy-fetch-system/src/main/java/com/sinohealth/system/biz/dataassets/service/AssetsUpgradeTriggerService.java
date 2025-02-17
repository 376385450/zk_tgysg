package com.sinohealth.system.biz.dataassets.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-11 13:35
 */
public interface AssetsUpgradeTriggerService {

    /**
     * 删除被标记为delete状态的资产表
     */
    void deleteCkTable();

    void convertToSnapshotTable(Integer max, Long userId);

    void markDeleteAssets(Integer month);

    void scheduleWideTable();

    //    void scheduleAssetsUpgrade();
    void schedulerRunFlow();

    AjaxResult<Void> manualUpgrade(Long assetsId);

    void createAssetsCompare(AssetsWideUpgradeTrigger trigger, Integer lastVersion);
}
