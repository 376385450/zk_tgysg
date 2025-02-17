package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author manleo
 * @Date 2020/7/1 16:36
 * @Version 1.0
 */
public class DataPlatformEnum {

    public static final String ALL = "all";
    public static final String ALL_CODE = "*";
    public static final String COMMA = ",";

    // 发布类型
    public static final Integer PUBLISH_TYPE_ADD = 1;
    public static final Integer PUBLISH_TYPE_UPDATE = 2;

    // 数据状态
    public static final String DELETE = "1";
    public static final String NORMAL = "0";

    public enum DataSourceType implements BaseDataPlatformCode {
        SINGLE_TABLE("1", "单表数据源"),
        SQL_DATALIST("2", "SQL数据集");
        private String code;
        private String msg;

        DataSourceType(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMsg() {
            return msg;
        }
    }

    public enum TableType implements BaseDataPlatformCode {
        STATIC("0", "静态"),
        DYNAMIC("1", "动态");
        private String code;
        private String msg;

        TableType(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMsg() {
            return msg;
        }
    }

    public enum TableUpdateFrequency implements BaseDataPlatformCode {
        ACTUAL_TIME("1", "实时"),
        EVERY_HOUR("2", "每小时"),
        EVERY_DAY("3", "每日"),
        NONE_UPDATE("4", "不更新");
        private String code;
        private String msg;

        TableUpdateFrequency(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMsg() {
            return msg;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class RequestParam {
        private Integer pageSize;
        private Integer pageNum;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class TableInfo {
        private String id;
        private String tableName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class TableFieldInfo {
        private String id;
        private String fieldName;
    }

}
