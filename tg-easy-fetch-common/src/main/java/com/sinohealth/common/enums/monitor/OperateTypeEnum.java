package com.sinohealth.common.enums.monitor;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 1:36 下午
 */
public enum OperateTypeEnum {

    CREATE("CREATE", "新增"),
    UPDATE("UPDATE", "更新"),
    QUERY("QUERY", "查询"),
    DELETE("DELETE", "删除");

    private String type;
    private String msg;

    OperateTypeEnum(String type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
