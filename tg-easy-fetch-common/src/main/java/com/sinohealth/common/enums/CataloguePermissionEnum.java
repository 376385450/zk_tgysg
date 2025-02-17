package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2023/8/16
 */
@Getter
@AllArgsConstructor
public enum CataloguePermissionEnum {


    // 部门
    DEPT(1),
    // 用户
    USER(2),
;

    Integer type;

}
