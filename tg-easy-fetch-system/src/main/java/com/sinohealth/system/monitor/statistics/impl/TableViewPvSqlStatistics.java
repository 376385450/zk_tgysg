package com.sinohealth.system.monitor.statistics.impl;

import com.sinohealth.system.monitor.statistics.SqlStatistics;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-13 12:47 下午
 */
public class TableViewPvSqlStatistics extends SqlStatistics<Integer> {

    private Long tableId;

    private static final String sql_template = "select count(*) pv from `event_log` where `operate_type` = 'QUERY' and `subject_id` = '%s' and `subject_type` = 'map' and `second_subject_type` = 'map_table_view'";

    public TableViewPvSqlStatistics(Long tableId) {
        this.tableId = tableId;
    }

    @Override
    protected String sql() {
        return String.format(sql_template, tableId);
    }

}
