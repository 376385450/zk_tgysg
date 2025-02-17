package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @author Jingjun
 * @since 2021/6/21
 */
@Getter
public enum AccessType {
    readOnly(1, "只读"),
    readExprot(2, "可读可导出"),
    contentEdit(3, "内容编辑"),
    metaDataEdit(4, "元数据编辑"),
    all(5, "管理权限");

    private int val;
    private String accessName;

    private AccessType(int val, String accessName) {
        this.val = val;
        this.accessName = accessName;
    }

    public static String getAccessTypeName(int val) {
        for (AccessType a : AccessType.values()) {
            if (a.val == val) {
                return a.accessName;
            }
        }
        return "";
    }

    public static AccessType getAccessType(int val) {
        for (AccessType a : AccessType.values()) {
            if (a.val == val) {
                return a;
            }
        }
        return null;
    }
}
