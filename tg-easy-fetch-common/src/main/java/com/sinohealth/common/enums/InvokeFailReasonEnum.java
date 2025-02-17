package com.sinohealth.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum InvokeFailReasonEnum {
    SUCCESS(0, "成功","SUCCESS"),
    SERVER_EXCEPTION(1, "服务器异常","SERVER_EXCEPTION"),
    WRONG_INPUT_PARAMETERS(2, "输入参数有误","WRONG_INPUT_PARAMETERS"),
    MAXIMUM_INPUT_EXCEEDED(3, "超过最大调用次数","MAXIMUM_INPUT_EXCEEDED")
    ;


    private int id;
    private String name;
    private String code;

    private InvokeFailReasonEnum(int id, String name,String code) {
        this.id = id;
        this.name = name;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static InvokeFailReasonEnum getEnum(Integer val) {
        for (InvokeFailReasonEnum a : InvokeFailReasonEnum.values()) {
            if (a.id == val) {
                return a;
            }
        }
        return null;
    }

    public static List<InvokeFailReasonEnum> getEnumList() {
        return Arrays.asList(InvokeFailReasonEnum.values());
    }

    public static String getStatusCode(Integer val) {
        for (InvokeFailReasonEnum a : InvokeFailReasonEnum.values()) {
            if (a.id == val) {
                return a.code;
            }
        }
        return null;
    }

    public static Integer getStatus(Integer val) {
        if(Objects.isNull(val)){
            return null;
        }
        for (InvokeFailReasonEnum a : InvokeFailReasonEnum.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }
}
