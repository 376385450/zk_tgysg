package com.sinohealth.common.enums.dataassets;

/**
 * 验收状态
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:28
 */
public enum AcceptanceStateEnum {

    /**
     * 待验收
     */
    wait,
    /**
     * 验收通过
     */
    pass,
    /**
     * 验收拒绝
     */
    reject,
    /**
     * 验收过期
     */
    version_roll
    ;
}
