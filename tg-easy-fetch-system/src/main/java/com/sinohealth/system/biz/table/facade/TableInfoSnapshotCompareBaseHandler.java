package com.sinohealth.system.biz.table.facade;

import com.sinohealth.system.domain.TableFieldInfo;

import java.util.List;
import java.util.Objects;

public abstract class TableInfoSnapshotCompareBaseHandler {
    // ----------------- sql 单一处理

    /**
     * 生成对比宽表插入语句
     *
     * @param wideTableExtraName 表名称
     * @return 对比宽表额外字段插入语句
     */
    public String buildInsertDiffWideExtraSql(String wideTableExtraName) {
        return "INSERT INTO " + wideTableExtraName + " VALUES (?,?,?,?,?,? )";
    }

    /**
     * 生成插入比对详细表sql
     *
     * @param tableName 表名
     * @return 插入比对详细表sql
     */
    public StringBuilder buildInsertDetailSql(String tableName, List<TableFieldInfo> fields) {
        StringBuilder inserSql = new StringBuilder();
        inserSql.append("INSERT INTO ");
        inserSql.append(tableName);
        inserSql.append(" VALUES( ?,?,");
        for (TableFieldInfo field : fields) {
            if (Objects.nonNull(field.getLogicKey()) && field.getLogicKey()) {
                inserSql.append("?,");
            }
        }
        inserSql.append(" ?,?,?,?,?,? )");
        return inserSql;
    }

    /**
     * 构建hive字段类型
     *
     * @param field 字段配置信息
     * @return hive字段类型
     */
    public String buildHiveDataType(TableFieldInfo field) {
        String dataType = field.getDataType();
        if (field.getLength() > 0 && field.getScale() > 0) {
            dataType = dataType + "(" + field.getLength() + "," + field.getScale() + ")";
        }
        if (dataType.equals("Date")) {
            dataType = "String";
        } else if (dataType.equals("Int64") || dataType.equals("Int32") || dataType.equals("Int16")) {
            dataType = "bigint";
        }
        // hive不支持该语法
        if (dataType.contains("Nullable")) {
            dataType = dataType.replaceFirst("Nullable\\(", "");
            dataType = dataType.replaceFirst("\\)", "");
        }
        return dataType;
    }

    /**
     * 构建ck字段类型
     *
     * @param field 字段配置信息
     * @return hive字段类型
     */
    public String buildCkDataType(TableFieldInfo field) {
        String dataType = field.getDataType();
        if (field.getLength() > 0 && field.getScale() > 0) {
            dataType = dataType + "(" + field.getLength() + "," + field.getScale() + ")";
        }
        if (dataType.equals("Date")) {
            dataType = "Nullable(String)";
        } else {
            dataType = "Nullable(" + dataType + ")";
        }
        return dataType;
    }
}
