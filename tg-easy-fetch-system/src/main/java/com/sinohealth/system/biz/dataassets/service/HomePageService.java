package com.sinohealth.system.biz.dataassets.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.homePage.*;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
public interface HomePageService {

    AjaxResult<List<HotAssetsDTO>> hotAssets(Integer type, Integer pageSize, Integer catalogId, Integer source);

    AjaxResult<DataStatistics> getAssetStatistics(Integer catalogId);

    AjaxResult<AssetDistribution> assetDistribution(Integer catalogId);

    // 二级目录资产分布
    AjaxResult<AssetDistribution> assetDistributions(Integer catalogId, Integer type);

    AjaxResult<List<LatestAsset>> latestAsset(Integer pageSize, Integer catalogId);

    AjaxResult<List<AssetApply>> assetApply(Integer catalogId);

    AjaxResult<AssetTypeStatistics> assetType(Integer catalogId, Integer type);

}
