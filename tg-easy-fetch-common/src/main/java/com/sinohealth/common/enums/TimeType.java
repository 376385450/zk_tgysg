package com.sinohealth.common.enums;

public enum TimeType {
    YEAR(1, "年",(long)1000 * 60 * 60 * 24 * 30 * 365),
    MONTH(2, "月",(long)1000 * 60 * 60 * 24 * 30),
    DAY(3, "日",(long)1000 * 60 * 60 * 24),
    HOURS(4, "小时",(long)1000 * 60 * 60),
    MINUTES (5, "分钟",(long)1000 * 60),
    SECONDS (6, "秒钟",(long)1000),
    ;

    private int id;
    private String name;
    private Long kd;

    private TimeType(int id, String name,Long kd) {
        this.id = id;
        this.name = name;
        this.kd = kd;
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

    public Long getKd() {
        return kd;
    }

    public void setKd(Long kd) {
        this.kd = kd;
    }

    public static String getTaskTypeName(int val) {
        for (TimeType a : TimeType.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return "";
    }

    public static long getTaskTypeKd(int val) {
        for (TimeType a : TimeType.values()) {
            if (a.id == val) {
                return a.kd;
            }
        }
        return 0;
    }
}
