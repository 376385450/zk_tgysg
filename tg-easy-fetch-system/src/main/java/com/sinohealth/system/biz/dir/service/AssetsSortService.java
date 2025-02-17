package com.sinohealth.system.biz.dir.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dir.dto.AssetsSortEditRequest;
import com.sinohealth.system.biz.dir.entity.DisplaySort;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-18 15:21
 */
public interface AssetsSortService {

    AjaxResult<Boolean> editSort(AssetsSortEditRequest request);

    void fillDefaultDisSort(DisplaySort entity);

}
