package com.sinohealth.system.util;

import cn.hutool.core.util.BooleanUtil;
import com.sinohealth.bi.constant.SqlConstant;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.MySql;
import com.sinohealth.bi.data.Table;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-05 14:19
 */
@Slf4j
public class QuerySqlUtil {

    public static void findOthersSql(List<FilterDTO> filters, StringBuffer whereSql) {
        for (FilterDTO dto : filters) {
            List<FilterDTO> dtoList = dto.getFilters();
            for (FilterDTO supDto : dtoList) {
                List<FilterDTO> childList = supDto.getFilterItem().getFilters();
                if (childList != null && childList.size() > 0) {
                    for (FilterDTO childDto : childList) {
                        Table table = new Table();
                        table.setUniqueId(1L);
                        table.setFactTable(true);

                        Filter targetFilter = new Filter();
                        ApplicationSqlUtil.convertToFilter(childDto, targetFilter);
                        final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), targetFilter);
                        whereSql.append(mySql.getWhereSql().replace("WHERE", dto.getLogicalOperator()));
                        findOthersSql(childList, whereSql);
                    }
                }
            }
        }
    }

    public static void findOthersSql(List<FilterDTO> filters, TableInfo tableInfo, StringBuffer whereSql) {
        for (FilterDTO dto : filters) {
            List<FilterDTO> dtoList = dto.getFilters();
            for (FilterDTO supDto : dtoList) {
                FilterDTO.FilterItemDTO filterItem = supDto.getFilterItem();
                if (Objects.isNull(filterItem)) {
                    continue;
                }
                List<FilterDTO> childList = filterItem.getFilters();
                if (CollectionUtils.isEmpty(childList)) {
                    continue;
                }

                for (FilterDTO childDto : childList) {
                    Table table = new Table();
                    table.setUniqueId(1L);
                    table.setTableId(tableInfo.getId());
                    table.setTableName(tableInfo.getTableName());
                    table.setFactTable(true);

                    Filter targetFilter = new Filter();
                    ApplicationSqlUtil.convertToFilter(childDto, targetFilter);
                    final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), targetFilter);
                    if (Objects.isNull(mySql.getWhereSql())) {
                        continue;
                    }

                    whereSql.append(mySql.getWhereSql().replace("WHERE", dto.getLogicalOperator()));
                    findOthersSql(childList, tableInfo, whereSql);
                }
            }
        }
    }

    public static String buildCondition(String colName, DataRangeQueryDto dataRangeQueryDto) {
        String search = dataRangeQueryDto.getSearchContent();
        if (StringUtils.isNotBlank(search)) {
            if (BooleanUtil.isTrue(dataRangeQueryDto.getExtractQuery())) {
                String eqPattern = " %s = '%s' ";
                if (StringUtils.contains(search, SqlConstant.IN_OR)) {
                    String[] parts = search.split(SqlConstant.IN_OR_REG);
                    String tuple = Stream.of(parts).filter(StringUtils::isNotBlank)
                            .map(v -> "'" + v + "'").collect(Collectors.joining(","));
                    if (StringUtils.isBlank(tuple)) {
                        return StringUtils.EMPTY;
                    }
                    return " " + String.format("%s IN (%s)", colName, tuple);
                } else {
                    return " " + String.format(eqPattern, colName, search);
                }
            } else {
                String sqlLikePattern = " %s LIKE '%%%s%%' ";
                if (StringUtils.contains(search, SqlConstant.IN_OR)) {
                    String[] parts = search.split(SqlConstant.IN_OR_REG);
                    String sql = Stream.of(parts)
                            .filter(StringUtils::isNotBlank)
                            .map(v -> String.format(sqlLikePattern, colName, v))
                            .collect(Collectors.joining(" " + SqlConstant.OR + " "));
                    if (StringUtils.isBlank(sql)) {
                        return StringUtils.EMPTY;
                    }
                    return " " + sql;
                } else {
                    return " " + String.format(sqlLikePattern, colName, search);
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public static String buildWhere(String tableName, List<FilterDTO.FilterItemDTO> needItems) {
        if (CollectionUtils.isEmpty(needItems)) {
            return "1=1";
        }
        List<String> conditions = new ArrayList<>();
        for (FilterDTO.FilterItemDTO needItem : needItems) {
            needItem.setTableAlias(tableName);
            Filter targetFilter = new Filter();
            FilterDTO filterDTO = new FilterDTO();
            filterDTO.setFilterItem(needItem);
            ApplicationSqlUtil.convertToFilter(filterDTO, targetFilter);
            Table table = new Table();
            table.setUniqueId(1L);
            table.setFactTable(true);
            ClickHouse clickHouse = new ClickHouse(Collections.singletonList(table), null, null, targetFilter, null, null);
            String sql = clickHouse.buildWhereByFilter(targetFilter);
            if (StringUtils.isBlank(sql)) {
                continue;
            }
            conditions.add(sql);
        }
        return String.join(" AND ", conditions);
    }

    public static String buildMySQLWhere(String tableName, List<FilterDTO.FilterItemDTO> needItems) {
        if (CollectionUtils.isEmpty(needItems)) {
            return "1=1";
        }
        List<String> conditions = new ArrayList<>();
        for (FilterDTO.FilterItemDTO needItem : needItems) {
            needItem.setTableAlias(tableName);
            Filter targetFilter = new Filter();
            FilterDTO filterDTO = new FilterDTO();
            filterDTO.setFilterItem(needItem);
            ApplicationSqlUtil.convertToFilter(filterDTO, targetFilter);
            Table table = new Table();
            table.setUniqueId(1L);
            table.setFactTable(true);
            MySql mysql = new MySql(Collections.singletonList(table), targetFilter);
            mysql.setHiddenQuote(true);
            String sql = mysql.buildWhereByFilter(targetFilter);
            if (StringUtils.isBlank(sql)) {
                continue;
            }
            conditions.add(sql);
        }
        return String.join(" AND ", conditions);
    }

}
