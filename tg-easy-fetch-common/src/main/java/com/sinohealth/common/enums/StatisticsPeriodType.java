package com.sinohealth.common.enums;

import java.util.Arrays;

/**
 * Author: ChenJiaRong
 * Date:   2021/7/30
 * Explain: 任务执行周期类型
 */
public enum StatisticsPeriodType {

    EVERY_DAY("01", "每天", "ss mm HH * * ?"),
    EVERY_MONDAY("02", "每个周一", "ss mm HH ? * 1"),
    EVERY_SUNDAY("03", "每个周日", "ss mm HH ? * 7"),
    THE_YEAR_ST_OF_EVERY_MONTH("04", "每个月最后一天", "ss mm HH L * ?"),
    THE_FIRST_OF_EVERY_MONTH("05", "每个月1号", "ss mm HH 1 * ?"),
    JANUARY_ONE_ST_EVERY_YEAR("06", "每年1月1号", "ss mm HH 1 1 ?");

    private String type;
    private String describe;
    private String cron;

    StatisticsPeriodType(String type, String describe, String cron) {
        this.type = type;
        this.describe = describe;
        this.cron = cron;
    }

    /**
     * 加入时间
     *
     * @param statisticsTime
     * @return
     */
    public String getCron(String statisticsTime) {
        String[] split = statisticsTime.split(":");
        return cron.replace("HH", split[0].trim())
                .replace("mm", split[1].trim())
                .replace("ss", "0").replace("00", "0");
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCron() {
        return cron;
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

    public static StatisticsPeriodType findType(String type) {

        return Arrays.stream(values()).filter(v -> v.getType().equals(type)).findFirst().orElse(null);
    }



}





