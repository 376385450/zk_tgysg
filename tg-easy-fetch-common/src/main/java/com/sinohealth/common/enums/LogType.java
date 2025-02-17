package com.sinohealth.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jingjun
 * @since 2021/5/13
 */
@Getter
public enum LogType {


    metadata_create(10, "元数据变更-新增"),
    metadata_update(11, "元数据变更-更改"),
    metadata_delete(12, "元数据变更-删除"),

    relation_create(20, "关联变更-新增"),
    relation_update(21, "关联变更-更改"),
    relation_delete(22, "关联变更-删除"),

    table_create(30, "表单变更-新增"),
    table_update(31, "表单变更-更改"),
    table_delete(32, "表单变更-删除"),
    table_copy(33, "表单变更-复制"),

    data_create(40, "数据变更-新增"),
    data_update(41, "数据变更-更改"),
    data_delete(42, "数据变更-删除"),
    data_query(43, "数据变更-查询"),
    data_export(44, "数据变更-导出");
    /**
     * 所有元数据变更
     */
    public static List<Integer> METADATA_ALL = new ArrayList<Integer>() {{
        add(metadata_create.getVal());
        add(metadata_update.getVal());
        add(metadata_delete.getVal());
    }};
    /**
     * 所有关联变更
     */
    public static List<Integer> RELATION_ALL = new ArrayList<Integer>() {{
        add(relation_create.getVal());
        add(relation_update.getVal());
        add(relation_delete.getVal());
    }};
    /**
     * 所有表单变更
     */
    public static List<Integer> TABLE_ALL = new ArrayList<Integer>() {{
        add(table_create.getVal());
        add(table_update.getVal());
        add(table_delete.getVal());
        add(table_copy.getVal());
    }};
    /**
     * 所有数据变更
     */
    public static List<Integer> DATA_ALL = new ArrayList<Integer>() {{
        add(data_create.getVal());
        add(data_update.getVal());
        add(data_delete.getVal());
        add(data_query.getVal());
        add(data_export.getVal());
    }};


    private String name;
    private int val;

    private LogType(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public static String findName(int val) {
        for (LogType log : LogType.values()) {
            if (val == log.val) {
                return log.getName();
            }
        }
        return "";
    }

    public static LogType findLogType(int val) {
        for (LogType log : values()) {
            if (val == log.val) {
                return log;
            }
        }
        return null;
    }
}
