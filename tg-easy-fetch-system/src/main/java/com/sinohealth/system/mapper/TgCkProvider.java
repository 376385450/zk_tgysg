package com.sinohealth.system.mapper;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.dto.table_manage.MetaDataFieldInfo;
import com.sinohealth.system.util.QuerySqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-05-20 11:52
 * @Desc
 */
@Slf4j
public class TgCkProvider {

    public static final String sqlMainPattern = "SELECT DISTINCT %s FROM %s";
    public static final String sqlInPattern = " %s %s (%s) ";
    public static final String sqlOrderPattern = " ORDER BY %s %s ";
    public static final String sqlPagePattern = " LIMIT %d OFFSET %d ";

    public String selectTablesThatNeed2Update() {
        return "SELECT DISTINCT ck_table_name FROM tg_sync_info_shard FINAL WHERE ck_sync_state = 1 AND pg_sync_state = 0";
    }

    public String selectMaxTime(List<String> tables) {
        String tableCond = "";
        if (CollectionUtils.isNotEmpty(tables)) {
            tableCond = " AND ck_table_name in (" + tables.stream().map(v -> "'" + v + "'").collect(Collectors.joining(",")) + ") ";
        }
        return "select * from (SELECT ck_table_name, max(update_time) as update_time " +
                "FROM tg_sync_info_shard FINAL " +
                "WHERE ck_sync_state = 1 " +
                "  AND pg_sync_state = 0 " +
                tableCond +
                "GROUP BY ck_table_name)t where t.update_time!=''";
    }

    public String selectLastUpdateTime(String tableName) {
        return "SELECT update_time FROM tg_sync_info_shard FINAL WHERE ck_sync_state = 1 AND pg_sync_state = 0 AND ck_table_name = '" +
                tableName + "' ORDER BY update_time DESC LIMIT 1";
    }

    public String removeNeed2UpdateMessage(String tableName) {
        return "INSERT INTO tg_sync_info_shard " +
                "(ck_table_name, ck_sync_state, pg_sync_state, comment, update_time, Sign, Version) " +
                "VALUES('" + tableName + "', 1, 0, '', '', -1, 1)";
    }

    public String insertUpdateSuccessMessage(String tableName) {
        return "INSERT INTO tg_sync_info_shard " +
                "(ck_table_name, ck_sync_state, pg_sync_state, comment, update_time, Sign, Version) " +
                "VALUES('" + tableName + "', 1, 1, '', '', 1, 2)";
    }

    public String selectAllDataRangeFromCk(final MetaDataFieldInfo mdfi) {
        String sql = "SELECT DISTINCT %s FROM %s ";
        String tab = StringUtils.replaceLast(mdfi.getTableName(), "_local", "_shard");
        return String.format(sql, mdfi.getColName(), tab);
    }


    /**
     * 查询列数据（客户和内网数据源 都使用该方法）
     *
     * @see TgCkProvider#selectDataRangeCountFromCk 注意和总数需要一起改
     */
    public static String selectDataRangeFromCk(String colName, String tableName, final DataRangeQueryDto dataRangeQueryDto) {
        String isSelected = dataRangeQueryDto.getIsSelected();
        StringBuilder sqlBuilder = new StringBuilder();
        tableName = StringUtils.replaceLast(tableName, "_local", "_shard");
        String search = dataRangeQueryDto.getSearchContent();
        List<String> data = dataRangeQueryDto.getData();
        String order = dataRangeQueryDto.getSearchOrder() == 1 ? "ASC" : "DESC";
        int start = (dataRangeQueryDto.getPageNum() - 1) * (dataRangeQueryDto.getPageSize());
        int pageSize = dataRangeQueryDto.getPageSize();

        sqlBuilder.append(String.format(sqlMainPattern, colName, tableName));
        sqlBuilder.append(" WHERE 1=1 ");

        if (StringUtils.isBlank(isSelected)) {
            // 全部标签
        } else if (isSelected.equals("1")) {
            // 已勾选
            if (!data.isEmpty()) {
                sqlBuilder.append(" AND ").append(String.format(sqlInPattern, colName, " IN ",
                        "'" + StringUtils.join(dataRangeQueryDto.getData(), "','") + "'"));
            }
        } else {
            // 未勾选
            if (!data.isEmpty()) {
                sqlBuilder.append(" AND ").append(String.format(sqlInPattern, colName, " NOT IN ",
                        "'" + StringUtils.join(dataRangeQueryDto.getData(), "','") + "'"));
            }
        }
        if (StringUtils.isNotBlank(search)) {
            String condition = QuerySqlUtil.buildCondition(colName, dataRangeQueryDto);
            if (StringUtils.isNotBlank(condition)) {
                sqlBuilder.append(" AND (").append(condition).append(")");
            }
        }

        // 级联查询实现
        if (CollectionUtils.isNotEmpty(dataRangeQueryDto.getFilterItems())) {
            String targetTable = StringUtils.replaceLast(dataRangeQueryDto.getTargetTable(), "_local", "_shard");
            String cascade = QuerySqlUtil.buildWhere(targetTable, dataRangeQueryDto.getFilterItems());
            if (StringUtils.isNotBlank(cascade)) {
                sqlBuilder.append(" AND ").append(cascade);
            }
        }

        sqlBuilder.append(String.format(sqlOrderPattern, colName, order));
        sqlBuilder.append(String.format(sqlPagePattern, pageSize, start));

        log.info("column sql={}", sqlBuilder);
        return sqlBuilder.toString();
    }

    public static String selectDataRangeCountFromCk(final String colName, String tableName, final DataRangeQueryDto dataRangeQueryDto) {
        tableName = StringUtils.replaceLast(tableName, "_local", "_shard");
        String condition = QuerySqlUtil.buildCondition(colName, dataRangeQueryDto);
        if (StringUtils.isNotBlank(condition)) {
            condition = " WHERE " + condition;
        }

        // 级联查询实现
        if (CollectionUtils.isNotEmpty(dataRangeQueryDto.getFilterItems())) {
            String targetTable = StringUtils.replaceLast(dataRangeQueryDto.getTargetTable(), "_local", "_shard");
            String cascade = QuerySqlUtil.buildWhere(targetTable, dataRangeQueryDto.getFilterItems());
            if (StringUtils.isNotBlank(cascade)) {
                if (StringUtils.isNotBlank(condition)) {
                    condition = condition + " AND " + cascade;
                } else {
                    condition = " WHERE " + cascade;
                }
            }
        }

        return "SELECT COUNT(*) FROM ( " + " SELECT DISTINCT " + colName + " FROM " + tableName + condition + ") t";
    }

    public String selectFieldDataRangeFromCk(String colName, String tableName, final DataRangeQueryDto dataRange) {
        String isSelected = dataRange.getIsSelected();
        StringBuilder sqlBuilder = new StringBuilder();
        String search = dataRange.getSearchContent();
        List<String> data = dataRange.getData();
        String order = dataRange.getSearchOrder() == 1 ? "ASC" : "DESC";
        int start = (dataRange.getPageNum() - 1) * (dataRange.getPageSize());
        int pageSize = dataRange.getPageSize();

        sqlBuilder.append(String.format(sqlMainPattern, colName, tableName));
        sqlBuilder.append(" WHERE 1=1 ");

        if (StringUtils.isBlank(isSelected)) {
            // 全部标签
        } else if (isSelected.equals("1")) {
            // 已勾选
            if (!data.isEmpty()) {
                sqlBuilder.append(" AND ").append(String.format(sqlInPattern, colName, " IN ",
                        "'" + StringUtils.join(dataRange.getData(), "','") + "'"));
            }
        } else {
            // 未勾选
            if (!data.isEmpty()) {
                sqlBuilder.append(" AND ").append(String.format(sqlInPattern, colName, " NOT IN ",
                        "'" + StringUtils.join(dataRange.getData(), "','") + "'"));
            }
        }
        if (StringUtils.isNotBlank(search)) {
            String condition = QuerySqlUtil.buildCondition(colName, dataRange);
            if (StringUtils.isNotBlank(condition)) {
                sqlBuilder.append(" AND (").append(condition).append(")");
            }
        }
        sqlBuilder.append(String.format(sqlOrderPattern, colName, order));
        sqlBuilder.append(String.format(sqlPagePattern, pageSize, start));

        log.info("column sql={}", sqlBuilder);
        return sqlBuilder.toString();
    }

    public String countFieldDataRangeFromCk(String colName, String tableName, final DataRangeQueryDto dataRangeQueryDto) {
        String condition = QuerySqlUtil.buildCondition(colName, dataRangeQueryDto);
        if (StringUtils.isNotBlank(condition)) {
            condition = " WHERE " + condition;
        }
        return "SELECT COUNT(*) FROM ( " + " SELECT DISTINCT " + colName + " FROM " + tableName + condition + ") t";
    }

    public static String selectAllDataFromCk(String tableName) {
        return new SQL() {{
            SELECT("*")
                    .FROM(StringUtils.replaceLast(tableName, "_local", "_shard"));
        }}.toString();
    }

    public static String selectApplicationDataFromCk(final String sql, final String whereSql, final GetDataInfoRequestDTO requestDTO) {
//        if (StringUtils.isBlank(requestDTO.getSortingField()) && StringUtils.isBlank(requestDTO.getSortBy())) {
//            log.error("UNSTABLE: field={} sort={}", requestDTO.getSortingField(), requestDTO.getSortBy());
//        }
        return new SQL() {{
            this.SELECT("*").FROM("( " + sql + " ) t");
            if (StringUtils.isNotBlank(whereSql)) {
                this.WHERE(whereSql);
            }
            if (StringUtils.isNoneBlank(requestDTO.getSortingField()) && StringUtils.isNoneBlank(requestDTO.getSortBy())) {
                this.ORDER_BY("`" + requestDTO.getSortingField() + "` " + requestDTO.getSortBy());
            }
            this.LIMIT(requestDTO.getPageSize());
            this.OFFSET((long) (requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
        }}.toString();
    }

    public static String selectCountApplicationDataFromCk(final String sql, final String whereSql) {
        return new SQL() {{
            this.SELECT("COUNT(*)").FROM("( " + sql + " ) t");
            if (StringUtils.isNotBlank(whereSql)) {
                this.WHERE(whereSql);
            }
        }}.toString();
    }

    public static String countFromCk(final String sql) {
        return new SQL() {{
            this.SELECT("COUNT(*)").FROM("( " + sql + " ) t");
        }}.toString();
    }

    public static String countTable(String tableName) {
        return new SQL() {{
            this.SELECT("COUNT(*)").FROM(tableName);
        }}.toString();
    }

    public static String runSql(String sql) {
        return sql;
    }


}
