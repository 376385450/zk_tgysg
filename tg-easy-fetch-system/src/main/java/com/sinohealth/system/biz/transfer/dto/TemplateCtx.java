package com.sinohealth.system.biz.transfer.dto;

import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-15 16:55
 */
@Data
@Builder
public class TemplateCtx {

    private TgTemplateInfo templateInfo;
    TgAssetInfo asset;
    Map<Long, String> fieldDictNameMap;
    List<Long> metricsIds;
    Map<Long, String> metricsMap;
}
