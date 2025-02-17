package com.sinohealth.system.biz.application.util;

import com.sinohealth.common.utils.StringUtils;
import lombok.Data;

import java.util.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-26 15:32
 */
@Data
public class SqlBuilderContext {

    // 中间计算
    private Set<String> groupBySet = new HashSet<>();
    /**
     * 需要从group by 子句中移除的字段，因为这些字段需要被聚合展示
     */
    // 中间计算
    private Set<String> groupByRemoveSet = new HashSet<>();

    private List<String> colSqlBuilder = new ArrayList<>();
    /**
     * 指标id -> 模板指标字符串和
     */
    private Map<Long, String> tempMetrics = new HashMap<>();

    private StringBuilder joinSqlBuilder = new StringBuilder();
    private StringBuilder metricsSqlBuilder = new StringBuilder();

    private StringBuilder havingBuilder = new StringBuilder();

    private String templateWhere;
    private String templateTable;
    private String templateAlias;

    /**
     * 时间聚合 原始字段
     */
    private String applyPeriod;

    public String buildTempSQL() {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ").append(String.join(",", this.getColSqlBuilder()))
                .append(" FROM ").append(templateTable).append(" ").append(templateAlias);

        this.getGroupBySet().removeAll(this.getGroupByRemoveSet());
        String groupBy = StringUtils.join(this.getGroupBySet(), ",");

        appendIfNotBlank(result, "", this.getJoinSqlBuilder());
        appendIfNotBlank(result, " WHERE ", this.getTemplateWhere());
        appendIfNotBlank(result, " GROUP BY ", groupBy);
        appendIfNotBlank(result, " HAVING ", this.getHavingBuilder());

        return result.toString();
    }

    public String buildApplySQL(String templateSql, String where) {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ").append(String.join(",", colSqlBuilder))
                .append(" FROM (").append(templateSql).append(") template");

        this.getGroupBySet().removeAll(this.getGroupByRemoveSet());
        String groupBy = StringUtils.join(this.getGroupBySet(), ",");

        appendIfNotBlank(result, "", this.getJoinSqlBuilder());
        appendIfNotBlank(result, " WHERE ", where);
        appendIfNotBlank(result, " GROUP BY ", groupBy);
        appendIfNotBlank(result, " HAVING ", this.getHavingBuilder());
        return result.toString();
    }

    private void appendIfNotBlank(StringBuilder result, String prefix, CharSequence segment) {
        if (StringUtils.isNotBlank(segment)) {
            result.append(prefix).append(segment);
        }
    }

    public void addMetrics(Long metricsId, String select) {
        colSqlBuilder.add(select);
        tempMetrics.put(metricsId, select);
    }
}
