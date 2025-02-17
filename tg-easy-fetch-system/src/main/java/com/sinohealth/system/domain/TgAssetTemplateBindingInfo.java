package com.sinohealth.system.domain;

import lombok.Data;

import javax.validation.Valid;

/**
 * @Author Rudolph
 * @Date 2023-08-08 15:10
 * @Desc
 */
@Data
public class TgAssetTemplateBindingInfo {
    @Valid
    private TgAssetInfo tgAssetInfo;
    @Valid
    private TgTemplateInfo bindingData;
    /**
     * 确认模板版本升级
     */
    private Boolean confirmUpgrade;
}
