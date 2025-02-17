package com.sinohealth.system.biz.dataassets.constant;

/**
 * @author Kuangcp
 * 2025-01-18 16:01
 */
public enum AutoFlowTypeEnum {
    /**
     * 指定需求id
     */
    apply_id,
    /**
     * 指定范围
     */
    apply_range;

    public static AutoFlowTypeEnum of(String type) {
        for (AutoFlowTypeEnum value : values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
