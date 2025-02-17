package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2024/2/26
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum {

    // type : 1.代办 2.申请 3.资产更新 4.系统升级
    TODO(1),
    APPLICATION(2),
    ASSET_UPDATE(3),
    SYSTEM_UPDATE(4);

    private Integer type;

}
