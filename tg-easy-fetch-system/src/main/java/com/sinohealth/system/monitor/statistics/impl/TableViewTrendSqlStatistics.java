package com.sinohealth.system.monitor.statistics.impl;


import com.sinohealth.system.monitor.statistics.SqlStatistics;

import java.util.List;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-13 1:52 下午
 */
public class TableViewTrendSqlStatistics extends SqlStatistics<List<Map<String, Object>>> {

    private Long tableId;

    private static final String sql_template = "select count(*) as pv, count(distinct user_id) as uv,log_date from `event_log` where `operate_type` = 'QUERY' and `subject_id` = '%s' and `subject_type` = 'map' and `second_subject_type` = 'map_table_view' group by log_date";

    public TableViewTrendSqlStatistics(Long tableId) {
        this.tableId = tableId;
    }

    @Override
    protected String sql() {
        return String.format(sql_template, tableId);
    }
}
