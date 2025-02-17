package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2024/1/12
 */
@AllArgsConstructor
@Getter
public enum ProjectStatusEnum {

    OFFLINE(0),
    ONLINE(1),
    ;

    private Integer code;

}
