package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2024/3/15
 */
@Getter
@AllArgsConstructor
public enum AssetLinkTypeEnum {

    // 资产关联
    ASSET_LINK(1),

    // 指引链接
    GUIDE_LINK(2);

    private final Integer type;

}
