package com.sinohealth.system.mapper;

import com.sinohealth.common.utils.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * @Author Rudolph
 * @Date 2022-07-26 14:08
 * @Desc
 */

public class TgPgProvider {

    public static String selectTableApplicationMapping(Long applyId) {
        return new SQL() {{
            SELECT("*").FROM("tg_table_application_mapping_info")
                    .WHERE("application_id = " + applyId);
        }}.toString();
    }

    public static String runSql(String sql) {
        return sql;
    }

    public static String createCustomerApplyAuthTableIfNotExist() {
        return "CREATE TABLE if not exists tg_customer_apply_auth ( " +
                "id SERIAL," +
                "apply_id int4," +
                "user_id int4," +
                "out_table_name varchar(255)," +
                "auth_type varchar(10)," +
                "update_by int4," +
                "update_time varchar(255)," +
                "status int2," +
                "PRIMARY KEY (id)" +
                ")";
    }



    public static String selectCustomerApplyAuthStatus(Long applyId){
        String sql = "SELECT COUNT(*)  FROM tg_customer_apply_auth WHERE apply_id = " + applyId;

        return sql;
    }

    public static String updateCustomerApplyAuthStatus(Long applyId, Integer status){
        String sql = "UPDATE tg_customer_apply_auth " +
                "SET status = " + status +
                " WHERE apply_id = " + applyId;

        return sql;
    }

    public static String queryReportForm4Customer(Long userId) {
        return "SELECT t1.*, t2.data_volume FROM tg_customer_apply_auth t1 "
                + "LEFT JOIN tg_table_application_mapping_info t2 ON t1.apply_id = t2.application_id "
                + "WHERE status = 1 and user_id = " + userId;
    }

    public static String queryTableName(Long userId, Long applyId) {
        return "SELECT t2.current_pg_table_name FROM tg_customer_apply_auth t1 " +
                "LEFT JOIN tg_table_application_mapping_info t2 ON t1.apply_id = t2.application_id " +
                "WHERE status = 1 and user_id = " + userId + " and apply_id = " + applyId;
    }

    public static String queryTableSize(String tableName) {
        return "SELECT COUNT(*) FROM " + tableName;
    }


    public static String queryTableData(String tableName, Integer querySize, Integer offset) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);
        if (querySize != null) {
            sqlBuilder.append(" LIMIT ").append(querySize);
        }
        if (querySize != null && offset != null) {
            sqlBuilder.append(" OFFSET ").append(offset);
        }
        return sqlBuilder.toString();
    }

    public static String queryTableData4Checking(String tableName, String whereSql, Integer querySize, Integer offset, String sortBy, String sortField) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);
        if (StringUtils.isNotBlank(whereSql)) {
            sqlBuilder.append(" WHERE ").append(whereSql);
        }
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortField)) {
            sqlBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortBy);
        }
        if (querySize != null) {
            sqlBuilder.append(" LIMIT ").append(querySize);
        }
        if (querySize != null && offset != null) {
            sqlBuilder.append(" OFFSET ").append(offset);
        }
        return sqlBuilder.toString();
    }

    public static String queryTableSize4Checking(String tableName, String whereSql) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT COUNT(*) FROM ").append(tableName);
        if (StringUtils.isNotBlank(whereSql)) {
            sqlBuilder.append(" WHERE ").append(whereSql);
        }

        return sqlBuilder.toString();
    }

    public String queryTableHeaders(String tableName) {
        return "SELECT attname FROM  pg_class a , pg_attribute b " +
                "where a.relname = '"+ tableName +"' and a.oid = b.attrelid " +
                "and attname not in ('tableoid', 'cmax', 'xmax', 'cmin', 'xmin', 'ctid') " +
                "order by attnum";
    }

    public String queryTableHeadersAndType(String tableName) {
        return "SELECT attname colName, format_type(b.atttypid, b.atttypmod) colType FROM  pg_class a , pg_attribute b " +
                "where a.relname = '"+ tableName +"' and a.oid = b.attrelid " +
                "and attname not in ('tableoid', 'cmax', 'xmax', 'cmin', 'xmin', 'ctid') " +
                "order by attnum";
    }

}
