package com.sinohealth.system.biz.rangepreset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.rangepreset.dto.RangeTemplatePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangeTemplateUpsertRequest;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-31 15:22
 */
public interface RangeTemplatePresetService {

    AjaxResult<IPage<RangeTemplatePresetDTO>> pageQuery(RangePresetPageRequest request);

    AjaxResult<Void> upsert(RangeTemplateUpsertRequest request);

    AjaxResult<Void> deleteById(Long id);
}
