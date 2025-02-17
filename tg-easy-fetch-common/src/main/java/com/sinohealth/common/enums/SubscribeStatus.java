package com.sinohealth.common.enums;

import java.util.Objects;

public enum SubscribeStatus {
    /** 订阅状态：0--订阅成功  1--正在申请订阅   2-订阅失败 */
    IS_RELEASE(0, "订阅成功"),
    IS_ONLINE(1, "正在申请订阅"),
    IS_REJECTED(2, "订阅失败"),
    ;



    private int id;
    private String name;

    private SubscribeStatus(int id, String name) {
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
        for (SubscribeStatus a : SubscribeStatus.values()) {
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
        for (SubscribeStatus a : SubscribeStatus.values()) {
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
        if(val == SubscribeStatus.IS_RELEASE.getId()){
            return ApprovalStatus.IS_PASSED.getId();
        }
        if(val == SubscribeStatus.IS_ONLINE.getId()){
            return ApprovalStatus.IS_PENDING.getId();
        }
        if(val == SubscribeStatus.IS_REJECTED.getId()){
            return ApprovalStatus.IS_REJECTED.getId();
        }
        return null;
    }
}
