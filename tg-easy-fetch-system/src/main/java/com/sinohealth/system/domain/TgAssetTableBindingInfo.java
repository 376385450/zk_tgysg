package com.sinohealth.system.domain;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2023-08-08 15:10
 * @Desc
 */
@Data
public class TgAssetTableBindingInfo {
    private TgAssetInfo tgAssetInfo;
    private TableInfo bindingData;
    private TgMetadataInfo tgMetadataInfo;
}
