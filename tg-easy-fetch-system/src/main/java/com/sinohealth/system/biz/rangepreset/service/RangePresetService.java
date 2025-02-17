package com.sinohealth.system.biz.rangepreset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.rangepreset.domain.base.CommonPreset;
import com.sinohealth.system.biz.rangepreset.dto.RangePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetUpsertRequest;
import com.sinohealth.system.domain.TgTemplateInfo;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 14:36
 */
public interface RangePresetService {

    AjaxResult<IPage<RangePresetDTO>> pageQuery(RangePresetPageRequest request);

    AjaxResult<Void> upsert(RangePresetUpsertRequest request);

    AjaxResult<String> queryByTempId(Long templateId);

    AjaxResult<Void> deleteById(Long id);

    Pair<List<TgTemplateInfo>, TgTemplateInfo> queryRelationTemp(Long templateId);

    void handleWideTemplate(Map<Long, String> tempUseMap, Map<Long, TgTemplateInfo> templateMap, List<? extends CommonPreset> wide);

    void handleScheduler(Map<Long, String> tempUseMap, List<? extends CommonPreset> scheduler);
}
