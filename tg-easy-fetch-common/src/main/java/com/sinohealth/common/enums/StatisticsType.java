package com.sinohealth.common.enums;

import java.util.Arrays;

/**
 * @program:
 * @description: 统计类型枚举
 * @author: ChenJiaRong
 * @date: 2021/8/2
 **/
public enum StatisticsType {

    TOTAL_QUANTITY_FORMED("01", "表单的总体量和表单总行数", "sysStatisticalRulesService.statisticsTask");

    private String type;
    private String describe;
    private String method;

    StatisticsType(String type, String describe, String method) {
        this.type = type;
        this.describe = describe;
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public static StatisticsType findType(String type) {

        return Arrays.stream(values()).filter(v -> v.getType().equals(type)).findFirst().orElse(null);
    }


}
