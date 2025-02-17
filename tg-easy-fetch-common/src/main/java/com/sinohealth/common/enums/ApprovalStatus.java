package com.sinohealth.common.enums;

import java.util.Objects;

public enum ApprovalStatus {
    IS_REJECTED(0, "驳回","0"),
    IS_PASSED(1, "通过","1"),
    IS_PENDING(2, "待审","2"),
    IS_WITHDRAWN(3, "撤销","3"),
    ;


    private int id;
    private String name;
    private String label;

    private ApprovalStatus(int id, String name,String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static String getLabel(Integer val) {
        for (ApprovalStatus a : ApprovalStatus.values()) {
            if (a.id == val) {
                return a.label;
            }
        }
        return null;
    }

    public static String getStatusName(Integer val) {
        for (ApprovalStatus a : ApprovalStatus.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return null;
    }

    public static Integer getStatus(Integer val) {
        if(Objects.isNull(val)){
            return null;
        }
        for (ApprovalStatus a : ApprovalStatus.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }


}
