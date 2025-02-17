package com.sinohealth.common.enums;

import java.util.Objects;

public enum LimitRuleEnum {
    //限制规则单位(0不限制,1年,2月,3周,4日,5时,6分,7秒)
    NONE(0, "不限制", -1, "不限制"),
    YEAR(1, "年", 365 * 24 * 60 * 60, "每年"),
    MONTH(2, "月", 30 * 24 * 60 * 60, "每月"),
    WEEK(3, "周", 7 * 24 * 60 * 60, "每周"),
    DAY(4, "日", 24 * 60 * 60, "每日"),
    HOURS(5, "小时", 60 * 60, "每小时"),
    MINUTES(6, "分钟", 60, "每分钟"),
    SECONDS(7, "秒钟", 1, "每秒"),
    ;

    private int id;
    private String name;
    private Integer unit;
    private String label;

    private LimitRuleEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private LimitRuleEnum(int id, String name, Integer unit, String label) {
        this.id = id;
        this.name = name;
        this.unit = unit;
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


    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static String get(int val) {
        for (LimitRuleEnum a : LimitRuleEnum.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return "";
    }

    public static LimitRuleEnum getEnum(int val) {
        for (LimitRuleEnum a : LimitRuleEnum.values()) {
            if (a.id == val) {
                return a;
            }
        }
        return LimitRuleEnum.NONE;
    }

    public static boolean checkLimitRuleUnit(int source, int sourceNum, int target, int targetNum) {
        LimitRuleEnum limitRuleEnum1 = getEnum(source);
        if (Objects.isNull(limitRuleEnum1)) {
            return false;
        }
        LimitRuleEnum limitRuleEnum2 = getEnum(target);
        if (Objects.isNull(limitRuleEnum2)) {
            return true;
        }
        if (limitRuleEnum1.id == LimitRuleEnum.NONE.id || sourceNum == 0) {
            return false;
        }
        if (limitRuleEnum2.id == LimitRuleEnum.NONE.id || targetNum == 0) {
            return true;
        }
        double limitRuleUnit1 = limitRuleEnum1.getUnit() / (double) sourceNum;
        double limitRuleUnit2 = limitRuleEnum2.getUnit() / (double) targetNum;
        return limitRuleUnit1 >= limitRuleUnit2 ? true : false;
    }

    public static String getLimitRule(int source, int sourceNum) {
        LimitRuleEnum limitRuleEnum1 = getEnum(source);
        if (limitRuleEnum1.id == LimitRuleEnum.NONE.id || sourceNum == 0) {
            return limitRuleEnum1.getLabel();
        }
        return limitRuleEnum1.getLabel() + sourceNum + "次";
    }



}
