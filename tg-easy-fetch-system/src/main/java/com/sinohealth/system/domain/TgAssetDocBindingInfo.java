package com.sinohealth.system.domain;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2023-08-08 15:10
 * @Desc
 */
@Data
public class TgAssetDocBindingInfo {
    private TgAssetInfo tgAssetInfo;
    private TgDocInfo bindingData;
}
