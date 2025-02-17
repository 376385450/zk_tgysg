package com.sinohealth.common.enums;

public enum TaskType {
    EXPORT(1, "数据表导出"),
    COPY(2, "数据表复制"),
    ;

    private int id;
    private String name;

    private TaskType(int id, String name) {
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

    public static String getTaskType(int val) {
        for (TaskType a : TaskType.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return "";
    }
}
