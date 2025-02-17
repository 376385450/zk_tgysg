package com.sinohealth.common.enums;

public enum SpeedOfProgressType {
    ERROR(0, "失败"),
    SUCCESS(1, "成功"),
    RUNNING(2, "进行中"),
    ;

    private int id;
    private String name;

    private SpeedOfProgressType(int id, String name) {
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

    public static String getSpeedOfProgressType(int val) {
        for (SpeedOfProgressType a : SpeedOfProgressType.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return "";
    }
}
