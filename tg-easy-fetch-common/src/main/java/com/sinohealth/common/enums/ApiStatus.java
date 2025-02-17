package com.sinohealth.common.enums;

import java.util.Objects;

public enum ApiStatus {
    IS_RELEASE(1, "发布（审核中）"),
    IS_ONLINE(2, "已上线（审核通过）"),
    IS_REJECTED(3, "已驳回（审核不通过）"),
            ;


    private int id;
    private String name;

    private ApiStatus(int id, String name) {
        this.id = id;
        this.name = name;
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

    public static String getStatusName(Integer val) {
        for (ApiStatus a : ApiStatus.values()) {
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
        for (ApiStatus a : ApiStatus.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }

    public static Integer toApprovalStatus(Integer val) {
        if(Objects.isNull(val)){
            return null;
        }
        if(val == ApiStatus.IS_RELEASE.getId()){
            return ApprovalStatus.IS_PENDING.getId();
        }
        if(val == ApiStatus.IS_ONLINE.getId()){
            return ApprovalStatus.IS_PASSED.getId();
        }
        if(val == ApiStatus.IS_REJECTED.getId()){
            return ApprovalStatus.IS_REJECTED.getId();
        }
        return null;
    }
}
