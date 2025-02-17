package com.sinohealth.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.sinohealth.bi.enums.DatabaseEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Impala 工具类
 *
 * @author linkaiwei
 * @date 2021/11/02 09:53
 * @since 1.5.0.0
 */
public class ImpalaUtils {

    private static final String driverName = DatabaseEnum.IMPALA.getDriver();
    // private static final String url = "jdbc:impala://192.168.52.21:21050/sinohealth_test;AuthMech=3;UID=data_tool_hadoop;PWD=data_tool_hadoop@#123abc";
    private static final String url = "jdbc:impala://192.168.52.21:21050/sinohealth_test;AuthMech=3;UID=data_tool_hadoop;PWD=123456";
    private static final String dbName = "sinohealth_test";
    private static final String username = "data_tool_hadoop";
    private static final String password = "data_tool_hadoop@#123abc";
    private static Connection con = null;
    private static Statement state = null;
    private static ResultSet res = null;

    private ImpalaUtils() {
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // main方法设置日志级别
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        loggerList.forEach(logger -> logger.setLevel(Level.INFO));


        Class.forName(driverName);
//        con = DriverManager.getConnection(url, username, password);
        con = DriverManager.getConnection(url);
        state = con.createStatement();

//        // 显示库
//        res = state.executeQuery("show databases");
//        while (res.next()) {
//            System.out.println(res.getString(1));
//        }
//        state.execute("USE sinohealth_test");
//        // 显示表
//        res = state.executeQuery("SHOW TABLES");
//        while (res.next()) {
//            System.out.println(res.getString(1));
//        }
//        //
//        res = state.executeQuery("desc formatted sinohealth_test.test_impala_table01");
//        while (res.next()) {
//            System.out.println(res.getString(1) + " - " + res.getString(2));
//        }

//        state.execute("use sinohealth_test");
//        // 创建表
//        Table table = new Table();
//        table.setTableAlias("测试创建表");
//        table.setTableName("sinohealth_test.test_impala_table01");
//        table.setFields(Arrays.asList(
//                new TableField().setFieldName("id").setDataType("INT").setFieldAlias("ID"),
//                new TableField().setFieldName("name").setDataType("STRING").setFieldAlias("名称"),
//                new TableField().setFieldName("age").setDataType("INT").setFieldAlias("年龄")));
//        table.setPartitions(Collections.singletonList(
//                new TableField().setFieldName("sex").setDataType("INT").setFieldAlias("性别")));
//        final String sql = createTable(table);
//        System.out.println(sql);
//        final boolean execute = state.execute(sql);
//        System.out.println(execute);

//        // 插入
//        Table table1 = new Table();
//        table1.setTableName("sinohealth_test.test_impala_table01");
//        table1.setFields(Arrays.asList(
//                new TableField().setFieldName("id").setDataType("INT").setFieldAlias("ID").setValue(1),
//                new TableField().setFieldName("name").setDataType("STRING").setFieldAlias("名称").setValue("张三"),
//                new TableField().setFieldName("age").setDataType("INT").setFieldAlias("年龄").setValue(22),
//                new TableField().setFieldName("sex").setDataType("INT").setFieldAlias("性别").setValue(1)));
//        final String insert = insert(table1);
//        System.out.println(insert);
//        final boolean execute1 = state.execute(insert);
//        System.out.println(execute1);

//        // 更新
//        Table table2 = new Table();
//        table2.setTableName("sinohealth_test.test_impala_table01");
//        table2.setFields(Arrays.asList(
//                new TableField().setFieldName("id").setDataType("INT").setFieldAlias("ID").setValue("2"),
//                new TableField().setFieldName("name").setDataType("STRING").setFieldAlias("名称").setValue("李四"),
//                new TableField().setFieldName("age").setDataType("INT").setFieldAlias("年龄").setValue("20")));
//        table2.setWhereFields(Arrays.asList(
//                new TableField().setFieldName("id").setDataType("INT").setFieldAlias("ID").setValue("2"),
//                new TableField().setFieldName("name").setDataType("STRING").setFieldAlias("名称").setValue("2"),
//                new TableField().setFieldName("age").setDataType("INT").setFieldAlias("年龄").setValue("2")));
//        final String update = update(table2);
//        System.out.println(update);
//        final boolean execute2 = state.execute(update);
//        System.out.println(execute2);

//        // 删除
//        Table table3 = new Table();
//        table3.setTableName("sinohealth_test.test_impala_table01");
//        table3.setWhereFields(Arrays.asList(
//                new TableField().setFieldName("id").setDataType("INT").setFieldAlias("ID").setValue("2"),
//                new TableField().setFieldName("name").setDataType("STRING").setFieldAlias("名称").setValue("2"),
//                new TableField().setFieldName("age").setDataType("INT").setFieldAlias("年龄").setValue("2")));
//        final String delete = delete(table3);
//        System.out.println(delete);
//        final boolean execute3 = state.execute(delete);
//        System.out.println(execute3);


//        // 查询
//        res = state.executeQuery("SELECT `id` FROM (SELECT row_number() OVER (ORDER BY id) AS row_number, t1.* FROM sinohealth_test.test_01 t1  ORDER BY id DESC  ) t  WHERE row_number >= 1 AND row_number <= 10");
//        while (res.next()) {
//            System.out.println(res.getString(1));
//        }

        // 查询表信息
//        final String descFormattedTable = descFormattedTable("sinohealth_test.ods_order_detail");
        final String descFormattedTable = "DESCRIBE sinohealth_test.ods_order_detail";
        res = state.executeQuery(descFormattedTable);
        while (res.next()) {
            System.out.println(res.getString(1) + " - " + res.getString(2) + " - " + res.getString(3));
        }
    }


    /**
     * 构建 Impala 创建表语句
     *
     * @param table 表信息
     * @return Impala 创建表语句
     * @author linkaiwei
     * @date 2021-10-20 10:09:51
     * @since 1.4.5.0
     */
    public static String createTable(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("CREATE TABLE IF NOT EXISTS %s (", table.getTableName()));

        // 字段
        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        fields.forEach(field -> sql.append(String.format("%s %s COMMENT '%s',", field.getFieldName(),
                String.format("%s%s", field.getDataType(),
                        field.getLength() == null || field.getLength() <= 0 ? "" : "(" + field.getLength() + ")"),
                field.getFieldAlias() == null ? "" : field.getFieldAlias())));
        sql.deleteCharAt(sql.length() - 1).append(")");

        // 分区
        final List<TableField> partitions = table.getPartitions();
        if (partitions != null && !partitions.isEmpty()) {
            sql.append(" PARTITIONED BY (");
            partitions.forEach(partition -> sql.append(String.format("%s %s COMMENT '%s',", partition.getFieldName(),
                    partition.getDataType(), partition.getFieldAlias() == null ? "" : partition.getFieldAlias())));
            sql.deleteCharAt(sql.length() - 1).append(")");
        }

        // 表注释
        if (table.getTableAlias() != null) {
            sql.append(String.format(" COMMENT '%s'", table.getTableAlias()));
        }

        return sql.toString();
    }

    /**
     * 构建 Impala 查询表信息语句
     *
     * @param tableName 表名称
     * @return Impala 查询表信息语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String descFormattedTable(String tableName) {
        if (tableName == null || tableName.trim().length() < 1) {
            return null;
        }

        return String.format("DESCRIBE FORMATTED %s", tableName);
    }

    /**
     * 构建 Impala 删除表语句
     *
     * @param tableName 表名称
     * @return Impala 删除表语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String dropTable(String tableName) {
        if (tableName == null || tableName.trim().length() < 1) {
            return null;
        }

        return String.format("DROP TABLE IF EXISTS %s", tableName);
    }

    /**
     * 构建 Impala 修改表列语句
     *
     * @param table         新地列信息
     * @param oldColumnName 旧表名称
     * @return Impala 修改表列语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String alterTableColumn(Table table, String oldColumnName) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("ALTER TABLE %s ", table.getTableName()));

        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        fields.forEach(field -> sql.append(String.format(" CHANGE COLUMN %s %s %s COMMENT '%s' ", oldColumnName,
                field.getFieldName(), field.getDataType(), field.getFieldAlias())));

        return sql.toString();
    }

    /**
     * 构建 Impala 新增表列语句
     *
     * @param table 新地列信息
     * @return Impala 新增表列语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String addTableColumn(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("ALTER TABLE %s ADD COLUMNS (", table.getTableName()));

        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        fields.forEach(field -> sql.append(String.format(" %s %s COMMENT '%s',",
                field.getFieldName(), field.getDataType(), field.getFieldAlias())));

        return sql.deleteCharAt(sql.length() - 1).append(")").toString();
    }

    /**
     * 构建 Impala 删除或者替换表列语句
     *
     * @param table 新地列信息
     * @return Impala 删除或者替换表列语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String replaceTableColumn(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("ALTER TABLE %s REPLACE COLUMNS (", table.getTableName()));

        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        fields.forEach(field -> sql.append(String.format(" %s %s COMMENT '%s',",
                field.getFieldName(), field.getDataType(), field.getFieldAlias())));

        return sql.deleteCharAt(sql.length() - 1).append(")").toString();
    }

    /**
     * 构建 Impala 修改表名称语句
     *
     * @param oldTableName 旧表名称
     * @param newTableName 新表名称
     * @return Impala 修改表名称语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String alterTableName(String oldTableName, String newTableName) {
        if (oldTableName == null || oldTableName.trim().length() < 1
                || newTableName == null || newTableName.trim().length() < 1) {
            return null;
        }

        return String.format("ALTER TABLE %s RENAME TO %s", oldTableName, newTableName);
    }


    /**
     * 构建 Impala 插入语句
     *
     * @param table 表信息
     * @return Impala 插入语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String insert(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("INSERT INTO TABLE %s (", table.getTableName()));

        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // 字段
        fields.forEach(field -> sql.append(field.getFieldName()).append(","));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");

        // 字段值
        fields.forEach(field -> {
            if (field.getValue() instanceof Number) {
                sql.append(field.getValue()).append(",");

            } else {
                sql.append("'").append(field.getValue()).append("',");
            }
        });
        sql.deleteCharAt(sql.length() - 1).append(")");

        return sql.toString();
    }

    /**
     * 构建 Impala 更新语句
     *
     * @param table 表信息
     * @return Impala 更新语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String update(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("UPDATE %s SET ", table.getTableName()));

        final List<TableField> fields = table.getFields();
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // 字段
        fields.forEach(field -> sql.append(String.format("%s = '%s',", field.getFieldName(), field.getValue())));
        sql.deleteCharAt(sql.length() - 1);

        // 过滤字段信息
        final List<TableField> whereFields = table.getWhereFields();
        if (whereFields != null && !whereFields.isEmpty()) {
            sql.append(" WHERE ");
            AtomicInteger index = new AtomicInteger(1);
            whereFields.forEach(field -> {
                if (index.get() == 1) {
                    sql.append(String.format("%s = '%s'", field.getFieldName(), field.getValue()));

                } else {
                    sql.append(String.format(" AND %s = '%s'", field.getFieldName(), field.getValue()));
                }
                index.getAndIncrement();
            });
        }

        return sql.toString();
    }

    /**
     * 构建 Impala 删除语句
     *
     * @param table 表信息
     * @return Impala 删除语句
     * @author linkaiwei
     * @date 2021-10-20 13:49:58
     * @since 1.4.5.0
     */
    public static String delete(Table table) {
        if (table == null || table.getTableName() == null || table.getTableName().trim().length() < 1) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("DELETE FROM %s", table.getTableName()));

        // 过滤字段信息
        final List<TableField> whereFields = table.getWhereFields();
        if (whereFields != null && !whereFields.isEmpty()) {
            sql.append(" WHERE ");
            AtomicInteger index = new AtomicInteger(1);
            whereFields.forEach(field -> {
                if (index.get() == 1) {
                    sql.append(String.format("%s = '%s'", field.getFieldName(), field.getValue()));

                } else {
                    sql.append(String.format(" AND %s = '%s'", field.getFieldName(), field.getValue()));
                }
                index.getAndIncrement();
            });
        }

        return sql.toString();
    }


    /**
     * 表信息
     *
     * @author linkaiwei
     * @date 2021/10/20 10:28
     * @since 1.4.5.0
     */
    @Data
    @Accessors(chain = true)
    public static class Table {

        /**
         * 表中文名
         */
        private String tableAlias;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 安全等级
         */
        private Integer safeLevel;

        /**
         * 备注
         */
        private String comment;

        /**
         * 字段信息
         */
        private List<TableField> fields;

        /**
         * 分区信息
         */
        private List<TableField> partitions;

        /**
         * 过滤字段信息
         */
        private List<TableField> whereFields;

    }

    /**
     * 字段信息
     *
     * @author linkaiwei
     * @date 2021/10/20 10:28
     * @since 1.4.5.0
     */
    @Data
    @Accessors(chain = true)
    public static class TableField {

        /**
         * 字段英文名
         */
        private String fieldName;

        /**
         * 字段中文名
         */
        private String fieldAlias;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 字段长度
         */
        private Integer length;

        /**
         * 值
         */
        private Object value;

    }

}
