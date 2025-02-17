package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.dto.assets.AssetDetail;
import com.sinohealth.system.dto.assets.AssetStatistics;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
public interface AssetInfoService {

    AjaxResult<AssetDetail> detail(Long Id);

    AjaxResult<AssetStatistics> statistics(Long id);

    List<AssetStatistics> statistics(List<Long> ids);

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 资产信息
     */
    TgAssetInfo getById(Long id);
}
