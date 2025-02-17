package com.sinohealth.system.biz.application.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.MySql;
import com.sinohealth.bi.data.Table;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.exception.ApplicationValidateException;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.exception.TemplateValidateException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SpringContextUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.common.utils.uuid.IdUtils;
import com.sinohealth.system.biz.application.bo.CustomFieldBuilderVO;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.ckpg.CkPgJavaDataType;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.*;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.mapper.CustomFieldInfoMapper;
import com.sinohealth.system.mapper.CustomFieldTemplateMapper;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.service.impl.TableFieldInfoServiceImpl;
import com.sinohealth.system.service.impl.TableInfoServiceImpl;
import com.sinohealth.system.service.impl.TemplateServiceImpl;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-08-15 9:02
 * @Desc
 * @see WideTableSqlBuilder 替换实现
 */
@Data
@Slf4j
@Deprecated
public class ApplicationSqlBuilder {

    private TgApplicationInfo applicationInfo;
    private boolean applyNeedGroupBy;
    private TgTemplateInfo templateInfo;
    private long templateUuid;
    private long applicationUuid;
    private Map<String, String> tableAliasMap = new HashMap<>();
    private Map<String, String> fieldAliasMap = new HashMap<>();

    // 中间计算
    private Set<String> groupBySet = new HashSet<>();
    /**
     * 需要从group by 子句中移除的字段，因为这些字段需要被聚合展示
     */
    // 中间计算
    private Set<String> groupByRemoveSet = new HashSet<>();
    private Set<String> templateMetrics = new HashSet<>();
    // 中间计算
    private Set<String> visitedCols = new HashSet<>();
    List<MetricsInfoDto> allMetrics = new LinkedList<>();

    // 中间计算
    private Set<Long> tableIdSet = new HashSet<>();
    private Map<Long, String> filedIdToNameMap = new HashMap<>();

    private Map<Long, String> filedIdToTemplateNameMap = new HashMap<>();
    private int idx = 0;
    /**
     * 用于标记 constructPeriodCol 实现与否, 用于规避同名标识执行多次导致SQL出错
     */
    private boolean periodTag;

    private StringBuilder colSqlBuilder = new StringBuilder();
    private StringBuilder joinSqlBuilder = new StringBuilder();
    private StringBuilder rangeSqlBuilder = new StringBuilder();
    private StringBuilder metricsSqlBuilder = new StringBuilder();

    private StringBuilder havingBuilder = new StringBuilder();
    private List<String> subQuerySql = TgCollectionUtils.newArrayList();

    private ITemplateService templateService;
    private ITableInfoService tableInfoService;
    private CustomFieldInfoMapper customMapper;
    private ITableFieldInfoService fieldInfoService;
    private CustomFieldTemplateMapper customFieldTemplateMapper;

    public ApplicationSqlBuilder() {
        this.templateService = SpringContextUtils.getBean(TemplateServiceImpl.class);
        this.tableInfoService = SpringContextUtils.getBean(TableInfoServiceImpl.class);
        this.customMapper = SpringContextUtils.getBean(CustomFieldInfoMapper.class);
        this.fieldInfoService = SpringContextUtils.getBean(TableFieldInfoServiceImpl.class);
        this.customFieldTemplateMapper = SpringContextUtils.getBean(CustomFieldTemplateMapper.class);
    }

    public ApplicationSqlBuilder(ITemplateService templateService, ITableInfoService tableInfoService, CustomFieldInfoMapper customMapper, ITableFieldInfoService fieldInfoService, CustomFieldTemplateMapper customFieldTemplateMapper) {
        this.templateService = templateService;
        this.tableInfoService = tableInfoService;
        this.customMapper = customMapper;
        this.fieldInfoService = fieldInfoService;
        this.customFieldTemplateMapper = customFieldTemplateMapper;
    }

    public String buildTemplateWithVerify(TgTemplateInfo template) {
        this.setTemplate(template);
        AtomicReference<String> groupBy = new AtomicReference<>("");
        AtomicReference<String> baseTable = new AtomicReference<>("");
        templateUuid = IdUtils.getUUID(CommonConstants.TEMPLATE + templateInfo.getCreateTime());
        this.prepareAliasMap(templateInfo.getTableInfo());

        this.hideAllCustomField(templateUuid);

        // 构建模板SQL
        String sql = this.combineTemplateSql(groupBy, baseTable, true);
        log.info("template sql={}", sql);

        return sql;
    }

    /**
     * 基于 维度，数据范围，聚合指标，日期聚合时间 构造最终SQL
     */
    public TgApplicationInfo buildApplication(TgApplicationInfo application) {
        this.setApplication(application);

        AtomicReference<String> groupBy = new AtomicReference<>();
        AtomicReference<String> baseTable = new AtomicReference<>();
        templateInfo = (TgTemplateInfo) getTemplateService().query(new HashMap() {{
            put(CommonConstants.ID, applicationInfo.getTemplateId());
        }});
        if (templateInfo != null) {
            templateUuid = IdUtils.getUUID(CommonConstants.TEMPLATE + templateInfo.getCreateTime());
        }
        applicationUuid = IdUtils.getUUID(CommonConstants.APPLICATION + applicationInfo.getCreateTime());

        prepareAliasMap();

        // 构建模板SQL
        String templateSql = combineTemplateSql(groupBy, baseTable, false);

        this.resetSQL();

        // 构建申请SQL 有子查询
        String asql = this.combineApplicationSql(groupBy, baseTable, templateSql);
        applicationInfo.setAsql(asql);

        // 构造申请SQL 无子查询
        // FIXME 目前嵌套子查询SQL构造过程中嵌入了数据的修改，需要拆分掉数据修改后才能完全两套实现 避免同一个类似流程互相影响
        if (Objects.equals(templateInfo.getSqlBuildMode(), ApplicationConst.SQL_MODE_SINGLE)) {
            this.resetSQL();
            this.visitedCols.clear();
            this.tableIdSet.clear();
            groupBy = new AtomicReference<>();
            baseTable = new AtomicReference<>();
            String applySQL = this.buildSingleLayerSQL(groupBy, baseTable);
            applicationInfo.setAsql(applySQL);
        }
        log.info("ASQL={}\n\n", applicationInfo.getAsql());

        applicationInfo = JsonBeanConverter.convert2Obj(applicationInfo);
        applicationInfo.setTableAliasMapping(tableAliasMap);
        applicationInfo = JsonBeanConverter.convert2Json(applicationInfo);
        return applicationInfo;
    }

    /**
     * TODO 单层，忽略模板内聚合指标的过滤。 group by 取子集
     */
    /**
     * @see this#combineTemplateSql
     */
    private String buildSingleLayerSQL(AtomicReference<String> groupBy, AtomicReference<String> baseTable) {
        StringBuilder result = new StringBuilder();
        allMetrics.addAll(templateInfo.getMetricsInfo());
        allMetrics.addAll(applicationInfo.getMetricsInfo());

        // 将申请的 period 信息传给模板, 因为单层SQL实际上运行的是模板内容
        templateInfo.setPeriodType(applicationInfo.getPeriodType());
        templateInfo.setPeriodField(applicationInfo.getPeriodField());

        constructPeriodCol(applicationInfo.getColsInfo(), allMetrics, CommonConstants.TEMPLATE);

        // 忽略模板的指标条件
        for (MetricsInfoDto metricsInfoDto : templateInfo.getMetricsInfo()) {
            metricsInfoDto.setContent(null);
        }
        constructMetrics(templateInfo.getMetricsInfo(), CommonConstants.TEMPLATE, false, true);
        constructMetrics(applicationInfo.getMetricsInfo(), CommonConstants.TEMPLATE, false, true);

        // 申请的字段集合小于模板 select 以及 group by
        constructSelectCol(applicationInfo.getColsInfo(), allMetrics, CommonConstants.TEMPLATE);

        String templateRangeSql = buildRangeSQL(templateInfo.getDataRangeInfo(), CommonConstants.TEMPLATE);
        if (StringUtils.isNoneBlank(templateRangeSql)) {
            rangeSqlBuilder.append(" ").append(templateRangeSql);
        }
        String applicationRangeSql = buildRangeSQL(applicationInfo.getApplyDataRangeInfo(), CommonConstants.TEMPLATE);
        if (StringUtils.isNoneBlank(applicationRangeSql)) {
            if (StringUtils.isNoneBlank(templateRangeSql)) {
                rangeSqlBuilder.insert(0, "(");
                rangeSqlBuilder.append(") AND ");
            }
            rangeSqlBuilder.append("(").append(applicationRangeSql).append(")");
        }

        constructJoin(templateInfo.getJoinInfo(), CommonConstants.TEMPLATE);
        constructJoin(applicationInfo.getJoinInfo(), CommonConstants.APPLICATION);

        groupBy.set(StringUtils.join(groupBySet, ","));

        groupBySet.removeAll(groupByRemoveSet);
        groupBy.set(StringUtils.join(groupBySet, ","));
        String tableName = Optional.ofNullable(applicationInfo).map(TgApplicationInfo::getBaseTableId)
                .map(getTableInfoService()::getDetail).map(TableInfoManageDto::getTableNameDistributed).orElse("");
        baseTable.set(tableName);

        result.append("SELECT ").append(colSqlBuilder).append(" FROM ").append(baseTable.get()).append(" ")
                .append(tableAliasMap.get(baseTable.get()));

        if (joinSqlBuilder.length() > 0) {
            result.append(joinSqlBuilder);
        }
        if (rangeSqlBuilder.length() > 0) {
            result.append(" WHERE ").append(rangeSqlBuilder);
        }
        if (!groupBy.get().isEmpty()) {
            result.append(" GROUP BY ").append(groupBy.get());
        }
        if (havingBuilder.length() > 0) {
            result.append(" HAVING ").append(havingBuilder);
        }

        return result.toString();
    }

    public String combineApplicationSql(AtomicReference<String> groupBy, AtomicReference<String> baseTable, String templateSql) {
        StringBuilder result = new StringBuilder();
        allMetrics.addAll(applicationInfo.getMetricsInfo());

        this.clearApplicationRecordFromTemplate();

        periodTag = true;

        constructPeriodCol(applicationInfo.getColsInfo(), allMetrics, CommonConstants.APPLICATION);
        constructMetrics(applicationInfo.getMetricsInfo(), CommonConstants.APPLICATION, false, false);
        // 构造 select 字段
        constructSelectCol(applicationInfo.getColsInfo(), allMetrics, CommonConstants.APPLICATION);
        constructJoin(applicationInfo.getJoinInfo(), CommonConstants.APPLICATION);
        appendRangeSQL(applicationInfo.getApplyDataRangeInfo(), CommonConstants.APPLICATION);

        groupBySet.removeAll(groupByRemoveSet);
        // 指标不应groupby 维度才需要
//        groupBySet.addAll(templateMetrics);
        groupBy.set(StringUtils.join(groupBySet, ","));

        ITableInfoService infoService = getTableInfoService();
        if (Objects.nonNull(infoService)) {
            baseTable.set(infoService.getDetail(applicationInfo.getBaseTableId()).getTableNameDistributed());
        }

        result.append("SELECT ").append(colSqlBuilder).append(" FROM ").append("(").append(templateSql).append(") ").append("template");

        if (joinSqlBuilder.length() > 0) {
            result.append(joinSqlBuilder);
        }
        if (rangeSqlBuilder.length() > 0) {
            result.append(rangeSqlBuilder);
        }
        boolean applyHasMetric = applicationInfo.getMetricsInfo().stream()
                .anyMatch(m -> m.getIsItself() == CommonConstants.APPLICATION);
        if (groupBy.get().length() > 0 && (applyHasMetric || applyNeedGroupBy)) {
            result.append(" GROUP BY ").append(groupBy.get());
        }
        if (havingBuilder.length() > 0) {
            result.append(" HAVING ").append(havingBuilder);
        }
        return result.toString();
    }

    public String combineTemplateSql(AtomicReference<String> groupBy, AtomicReference<String> baseTable,
                                     boolean createTemplate) {
        StringBuilder result = new StringBuilder();
        allMetrics.addAll(templateInfo.getMetricsInfo());

        constructPeriodCol(templateInfo.getColsInfo(), allMetrics, CommonConstants.TEMPLATE);
        constructMetrics(templateInfo.getMetricsInfo(), CommonConstants.TEMPLATE, createTemplate, false);

        constructSelectCol(templateInfo.getColsInfo(), allMetrics, CommonConstants.TEMPLATE);
        this.appendRangeSQL(templateInfo.getDataRangeInfo(), CommonConstants.TEMPLATE);
        constructJoin(templateInfo.getJoinInfo(), CommonConstants.TEMPLATE);
        groupBySet.removeAll(groupByRemoveSet);
        groupBy.set(StringUtils.join(groupBySet, ","));

        String tableName = Optional.ofNullable(templateInfo).map(TgTemplateInfo::getBaseTableId)
                .map(getTableInfoService()::getDetail).map(TableInfoManageDto::getTableNameDistributed).orElse("");
        baseTable.set(tableName);

        result.append("SELECT ").append(colSqlBuilder).append(" FROM ").append(baseTable.get()).append(" ")
                .append(tableAliasMap.get(baseTable.get()));

        if (joinSqlBuilder.length() > 0) {
            result.append(joinSqlBuilder);
        }
        if (rangeSqlBuilder.length() > 0) {
            result.append(rangeSqlBuilder);
        }
        if (groupBy.get().length() > 0 && templateInfo.getMetricsInfo().size() > 0) {
            result.append(" GROUP BY ").append(groupBy.get());
        }
        if (havingBuilder.length() > 0) {
            result.append(" HAVING ").append(havingBuilder);
        }

        return result.toString();
    }

    private void clearApplicationRecordFromTemplate() {
        applicationInfo.getJoinInfo().removeIf(x -> x.getIsItself() == null || x.getIsItself() == 1);
        applicationInfo.getMetricsInfo().removeIf(x -> x.getIsItself() == null || x.getIsItself() == 1);
    }

    private void prepareAliasMap() {
        if (templateInfo != null) {
            prepareAliasMap(templateInfo.getTableInfo());
        }

        prepareAliasMap(applicationInfo.calcTableInfo());
        applicationInfo.getJoinInfo().forEach(j -> {
            TableInfoManageDto detail1 = getTableInfoService().getDetail(j.getTableId1());
            TableInfoManageDto detail2 = getTableInfoService().getDetail(j.getTableId2());
            prepareAliasMap(detail1.getTableNameDistributed());
            prepareAliasMap(detail2.getTableNameDistributed());
        });
    }

    private void prepareAliasMap(String tables) {
        for (String t : tables.split(",")) {
            String st = StringUtils.replaceLast(t, "_shard", "_local");
            String tn = StringUtils.replaceLast(t, "_local", "_shard");
            if (!tableAliasMap.containsKey(tn)) {
                tableAliasMap.put(tn, "t_" + ++idx);
            }
            getFieldInfoService().getFieldsByTableName(st)
                    .forEach(f -> fieldAliasMap.put(st + "." + f, tableAliasMap.get(tn) + "_" + f));
        }
    }

    private void setApplication(TgApplicationInfo applicationInfo) {
        this.applicationInfo = JsonBeanConverter.convert2Json(applicationInfo);
    }

    private void setTemplate(TgTemplateInfo templateInfo) {
        this.templateInfo = JsonBeanConverter.convert2Json(templateInfo);
    }

    /**
     * 追加需求: 时间聚合功能
     * 根据申请中 period_field 和 period_type
     * 在出数的 SQL 中的 col 和 group by 中追加时间聚合字段
     *
     * @see ApplicationSqlBuilder#constructPeriodSeason 添加groupby子句字段的来源
     */
    private void constructPeriodCol(List<ColsInfoDto> colsInfoDtos, List<MetricsInfoDto> metricsInfoDtos, int type) {
        for (ColsInfoDto c : colsInfoDtos) {
            TableInfoManageDto detail = getTableInfoService().getDetail(c.getTableId());
            String tn = detail.getTableNameDistributed();

            if (Objects.nonNull(applicationInfo) && StringUtils.isNoneBlank(applicationInfo.getPeriodField())) {
                long id = Long.parseLong(applicationInfo.getPeriodField());
                TableFieldInfo field = getFieldInfoService().getById(id);
                CkPgJavaDataType ckType = CkPgJavaDataType.resolveDefaultArrayDataType(field.getDataType());
                StringBuilder temp = new StringBuilder();

                String fn = field.getFieldName();

                boolean isTemplate = type == CommonConstants.TEMPLATE;
                if (isTemplate) {
                    temp.append(tableAliasMap.get(tn)).append(".").append(fn);
                } else {
                    temp.append(tableAliasMap.get(tn)).append("_").append(fn);
                }

                if (isTemplate && StringUtils.isNotBlank(templateInfo.getPeriodType())
                        && templateInfo.getPeriodField().equals(field.getId().toString())) {
                    if (!ckType.equals(CkPgJavaDataType.Date)
                            && !ckType.name().startsWith(CkPgJavaDataType.DateTime.name())) {
                        throw new CustomException("配置错误: 时间指标聚合字段非日期类型");
                    } else {
                        this.appendPeriodFields(templateInfo.getPeriodType(), temp, colSqlBuilder);
                        appendCommas(colSqlBuilder);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_NEW);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_STR);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_TYPE);
                        groupByRemoveSet.add(tableAliasMap.get(tn) + "." + fn);
                        fieldAliasMap.put(tn + "." + fn, "period");
                        visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                    }
                }

                if (type == CommonConstants.APPLICATION && StringUtils.isNotBlank(applicationInfo.getPeriodType())
                        && applicationInfo.getPeriodField().equals(field.getId().toString())
                        && periodTag) {
                    if (!ckType.equals(CkPgJavaDataType.Date)
                            && !ckType.name().startsWith(CkPgJavaDataType.DateTime.name())) {
                        throw new CustomException("申请定义中: 日期聚合字段非日期类型");
                    } else {
                        this.appendPeriodFields(applicationInfo.getPeriodType(), temp, colSqlBuilder);
                        appendCommas(colSqlBuilder);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_NEW);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_STR);
                        groupBySet.add(ApplicationConst.PeriodField.PERIOD_TYPE);
                        groupByRemoveSet.add(tableAliasMap.get(tn) + "_" + fn);
                        visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                        periodTag = false;
                    }
                }
            }

            c.getSelect().forEach((id) -> {
                if (metricsInfoDtos.stream()
                        .filter(v -> !Objects.equals(v.getComputeWay(), CommonConstants.ComputeWay.DIY))
                        .map(MetricsInfoDto::getColName).filter(Objects::nonNull)
                        .noneMatch(mid -> mid.longValue() == id.longValue())) {
                    String fn, ft;
                    try {
                        TableFieldInfo field = getFieldInfoService().getById(id);
                        fn = field.getFieldName();
                        ft = field.getDataType();

                        StringBuilder temp = new StringBuilder();

                        boolean isTemplate = type == CommonConstants.TEMPLATE;
                        if (isTemplate) {
                            temp.append(tableAliasMap.get(tn)).append(".").append(fn);
                        } else {
                            temp.append(tableAliasMap.get(tn)).append("_").append(fn);
                        }
//                        CkPgJavaDataType ckType = CkPgJavaDataType.resolveDefaultArrayDataType(ft);
//                        String typeName = ckType.name();
                        // 没有模板维度的时间聚合操作 但是单层SQL需要使用
//                        if (isTemplate && StringUtils.isNotBlank(templateInfo.getPeriodType())
//                                && templateInfo.getPeriodField().equals(field.getId().toString())) {
//                            if (!ckType.equals(CkPgJavaDataType.Date)
//                                    && !typeName.startsWith(CkPgJavaDataType.DateTime.name())) {
//                                throw new CustomException("配置错误: 时间指标聚合字段非日期类型");
//                            } else {
//                                this.appendPeriodFields(templateInfo.getPeriodType(), temp, colSqlBuilder);
//                                appendCommas(colSqlBuilder);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_NEW);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_STR);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_TYPE);
//                                groupByRemoveSet.add(tableAliasMap.get(tn) + "." + fn);
//                                fieldAliasMap.put(tn + "." + fn, "period");
//                                visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
//                            }
//                        }
//                        if (type == CommonConstants.APPLICATION && StringUtils.isNotBlank(applicationInfo.getPeriodType())
//                                && applicationInfo.getPeriodField().equals(field.getId().toString())
//                                && periodTag) {
//                            if (!ckType.equals(CkPgJavaDataType.Date)
//                                    && !typeName.startsWith(CkPgJavaDataType.DateTime.name())) {
//                                throw new CustomException("申请定义中: 日期聚合字段非日期类型");
//                            } else {
//                                this.appendPeriodFields(applicationInfo.getPeriodType(), temp, colSqlBuilder);
//                                appendCommas(colSqlBuilder);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_NEW);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_STR);
//                                groupBySet.add(ApplicationConst.PeriodField.PERIOD_TYPE);
//                                groupByRemoveSet.add(tableAliasMap.get(tn) + "_" + fn);
//                                visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
//                                periodTag = false;
//                            }
//                        }
                    } catch (Exception e) {
                        log.error("", e);
                        if (e instanceof CustomException) {
                            throw e;
                        }
                        CustomFieldInfo field = getCustomMapper().queryById(id);
                        if (field != null) {
                            fn = field.getFieldName();
                            if (!templateMetrics.contains(fn)) {
                                groupBySet.add(fn);
                                colSqlBuilder.append(fn);
                                appendCommas(colSqlBuilder);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * SQL追加 period_type period_str period_new
     */
    private void appendPeriodFields(String type, StringBuilder temp, StringBuilder colSqlBuilder) {
        if (type.equals(CommonConstants.YEAR)) {
            constructPeriodYear(temp, colSqlBuilder);
        }
        if (type.equals(CommonConstants.HFYEAR)) {
            constructPeriodHfyear(temp, colSqlBuilder);
        }
        if (type.equals(CommonConstants.SEASON)) {
            constructPeriodSeason(temp, colSqlBuilder);
        }
        if (type.equals(CommonConstants.MONTH)) {
            constructPeriodMonth(temp, colSqlBuilder);
        }
        if (type.equals(CommonConstants.DAYOFMONTH)) {
            constructPeriodDayOfMonth(temp, colSqlBuilder);
        }
    }

    private void constructPeriodDayOfMonth(StringBuilder temp, StringBuilder colSqlBuilder) {
        String template = " (toString(toYear(#)) || \n" +
                "multiIf(toMonth(#) <= '9', '0' || toString(toMonth(#)),\n" +
                "\t\ttoString(toMonth(#))) || multiIf(toDayOfMonth(#) <= '9', '0' || toString(toDayOfMonth(#)), toString(toDayOfMonth(#)))\n" +
                ") period_str, parseDateTimeBestEffort(period_str) as period_new, '日' as period_type ";
        appendPeriod(temp, colSqlBuilder, template);
    }

    private void constructPeriodMonth(StringBuilder temp, StringBuilder colSqlBuilder) {
        String template = " (toString(toYear(#)) || \n" +
                "multiIf(toMonth(#) <= '9', '0' || toString(toMonth(#)),\n" +
                "\t\ttoString(toMonth(#)))\n" +
                ") period_str, parseDateTimeBestEffort(period_str) as period_new, '月度' as period_type ";
        appendPeriod(temp, colSqlBuilder, template);
    }

    private void constructPeriodSeason(StringBuilder temp, StringBuilder colSqlBuilder) {
        String template = " (toString(toYear(#)) || \n" +
                "multiIf(toQuarter(#) = '1', 'Q1',\n" +
                "\t\ttoQuarter(#) = '2', 'Q2',\n" +
                "\t\ttoQuarter(#) = '3', 'Q3',\n" +
                "\t\t'Q4')\n" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(#)) || \n" +
                "multiIf(toQuarter(#) = '1', '01',\n" +
                "\t\ttoQuarter(#) = '2', '04',\n" +
                "\t\ttoQuarter(#) = '3', '07',\n" +
                "\t\t'10'))) as period_new, '季度' as period_type ";
        appendPeriod(temp, colSqlBuilder, template);
    }

    private void constructPeriodHfyear(StringBuilder temp, StringBuilder colSqlBuilder) {
        String template = " (toString(toYear(#)) || \n" +
                "multiIf(toMonth(#) <= '6', 'H1',\n" +
                "\t\t'H2')\n" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(#)) || \n" +
                "multiIf(toMonth(#) <= '6', '01',\n" +
                "\t\t'07')\n" +
                ")) as period_new, '半年度' as period_type ";
        appendPeriod(temp, colSqlBuilder, template);
    }

    private void constructPeriodYear(StringBuilder temp, StringBuilder colSqlBuilder) {
        String template = " toString(toYear(#)) period_str, parseDateTimeBestEffort(period_str) as period_new, '年度' as period_type ";
        appendPeriod(temp, colSqlBuilder, template);
    }

    private void appendPeriod(StringBuilder temp, StringBuilder colSqlBuilder, String template) {
        String underLineTemp = temp.toString().replace(".", "_");
        if (!visitedCols.contains(underLineTemp)) {
            colSqlBuilder.append(template.replace("#", temp.toString()));
        } else {
            colSqlBuilder.append(template.replace("#", underLineTemp));
        }
    }

    private void constructSelectCol(List<ColsInfoDto> colsInfoDtos, List<MetricsInfoDto> metricsInfoDtos, int type) {
        boolean isApplication = type == CommonConstants.APPLICATION;

        // 1. 普通原始字段，时间聚合字段
        for (ColsInfoDto c : colsInfoDtos) {
            TableInfoManageDto detail = getTableInfoService().getDetail(c.getTableId());
            String tn = detail.getTableNameDistributed();
            String localTn = detail.getTableName();
            c.getSelect().forEach((id) -> {
                if (metricsInfoDtos.stream()
                        .filter(v -> !Objects.equals(v.getComputeWay(), CommonConstants.ComputeWay.DIY))
                        .filter(v -> v.getComputeWay() < CommonConstants.ComputeWay.CUSTOM_START)
                        .map(MetricsInfoDto::getColName).filter(Objects::nonNull)
                        .noneMatch(mid -> mid.longValue() == id.longValue())) {
                    String fn;
                    String fid;
                    try {
                        TableFieldInfo field = getFieldInfoService().getById(id);
                        fn = field.getFieldName();
                        fid = field.getId().toString();


                        if (isApplication && StringUtils.isNoneBlank(applicationInfo.getPeriodField())
                                && Objects.equals(fn, "period")) {
                            log.warn("ignore apply period field");
                            return;
                        }
                        if (type == CommonConstants.TEMPLATE) {
                            filedIdToTemplateNameMap.put(id, tableAliasMap.get(tn) + "_" + fn);

                            colSqlBuilder.append(tableAliasMap.get(tn)).append(".").append(fn).append(" ").append(tableAliasMap.get(tn)).append("_").append(fn);
                            appendCommas(colSqlBuilder);
                            groupBySet.add(tableAliasMap.get(tn) + "." + fn);
                            fieldAliasMap.put(tn + "." + fn, tableAliasMap.get(tn) + "_" + fn);
                            visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                        }
                        if (isApplication) {
                            String afn = fieldAliasMap.get(localTn + "." + fn);
                            groupBySet.add(tableAliasMap.get(tn) + "_" + fn);
                            /* 如果不属于聚合时间字段 **/
                            if (!fid.equals(applicationInfo.getPeriodField())) {
                                if (c.getIsItself() == CommonConstants.APPLICATION) {
                                    colSqlBuilder.append(tableAliasMap.get(tn)).append(".").append(fn).append(" ").append(afn);
                                } else {
                                    colSqlBuilder.append(afn);
                                }
                            }
                            appendCommas(colSqlBuilder);
                            visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                        fn = getCustomMapper().queryById(id).getFieldName();
                        if (!templateMetrics.contains(fn)) {
                            groupBySet.add(fn);
                            colSqlBuilder.append(fn);
                            appendCommas(colSqlBuilder);
                        }
                    }
                }
            });
        }
        if (colSqlBuilder.length() > 0) {
            colSqlBuilder.deleteCharAt(colSqlBuilder.length() - 1);
        }

        // 2. 二次聚合指标 字段
        if (isApplication) {
            this.appendSelectedTemplateMetric();
        }
    }

    /**
     * 构造申请SQL时：追加选择的模板指标
     */
    private void appendSelectedTemplateMetric() {
        // 构造提数申请时：追加已选择的 提数模板中配置的聚合指标
        TemplateMetric templateMetric = Optional.ofNullable(applicationInfo)
                .map(TgApplicationInfo::getTemplateMetrics).orElse(null);
        if (Objects.isNull(templateMetric)) {
            return;
        }

        List<RealName> realNames = templateMetric.getRealName();
        if (CollectionUtils.isEmpty(realNames)) {
            return;
        }

        applyNeedGroupBy = true;
        List<String> fields = new ArrayList<>();
        Map<Long, CustomFieldTemplate> templateMap = new HashMap<>();
        // xx_temp -> xx
//        Map<String, String> funcToAliasMap = new HashMap<>();
        Function<RealName, String> buildAliasName = select -> StringUtils.isNotBlank(select.getRealName()) ? select.getRealName() : " " + select.getFieldName() + "_apply";

        // 二次聚合 普通聚合指标
        // eg: SUM(`t_1_tz_fdxse_sum`) `销售量_temp`
        for (RealName select : realNames) {
            CustomFieldInfo customFieldInfo = customMapper.queryById(select.getId());
            String aliasName = buildAliasName.apply(select);
            if (customFieldInfo.getComputeWay() > CommonConstants.ComputeWay.CUSTOM_START) {
                CustomFieldTemplate customFieldTemplate = customFieldTemplateMapper.selectById(customFieldInfo.getComputeWay());
                templateMap.put(select.getId(), customFieldTemplate);
            } else {
                Optional<CommonConstants.ComputeWayEnum> enumOpt = CommonConstants.ComputeWayEnum.getById(customFieldInfo.getComputeWay());
                if (enumOpt.isPresent()) {
                    CommonConstants.ComputeWayEnum e = enumOpt.get();
                    String funcName = e.buildApplyExpression("`" + select.getFieldName() + "`");

                    aliasName = ApplicationSqlUtil.trimMetricSuffix(aliasName);

                    String part = funcName + " `" + aliasName + "` ";
                    fields.add(part);
                }
            }
        }

        // 追加计算表达式 SUM(`A_temp`) / SUM(`B_temp`)
        for (RealName select : realNames) {
            CustomFieldTemplate customFieldTemplate = templateMap.get(select.getId());
            if (Objects.isNull(customFieldTemplate)) {
                continue;
            }
            String aliasName = buildAliasName.apply(select);
            String expression = customFieldTemplate.getApply();
            fields.add(expression + " `" + aliasName + "`");
        }

        String customField = String.join(",", fields);
        if (StringUtils.isNoneBlank(customField)) {
            if (StringUtils.isNoneBlank(colSqlBuilder)) {
                colSqlBuilder.append(",");
            }
            colSqlBuilder.append(customField);
        }
    }

    private void constructJoin(List<JoinInfoDto> joinInfoDtos, int type) {
        joinInfoDtos.stream().filter(v -> Objects.equals(v.getIsItself(), type)).forEach(j -> {
            String t1 = getTableInfoService().getDetail(j.getTableId1()).getTableNameDistributed();
            String t2 = getTableInfoService().getDetail(j.getTableId2()).getTableNameDistributed();
            String c1 = getFieldInfoService().getById(j.getJoinCol1()).getFieldName();
            String c2 = getFieldInfoService().getById(j.getJoinCol2()).getFieldName();

            String join = ApplicationConst.JoinType.TYPE_MAP.get(j.getJoinType());
            if (StringUtils.isBlank(join)) {
                throw new RuntimeException("不支持的 JOIN 方式");
            }
            joinSqlBuilder.append(join);

            String localT1 = StringUtils.replaceLast(t1, "_shard", "_local");

            joinSqlBuilder.append(t2).append(" ").append(tableAliasMap.get(t2));
            if (type == CommonConstants.TEMPLATE) {
                joinSqlBuilder.append(" ON ").append(tableAliasMap.get(t1)).append(".").append(c1).append(" = ")
                        .append(tableAliasMap.get(t2)).append(".").append(c2);
            }
            if (type == CommonConstants.APPLICATION) {
                joinSqlBuilder.append(" ON ").append("template.").append(fieldAliasMap.get(localT1 + "." + c1)).append(" = ")
                        .append(tableAliasMap.get(t2)).append(".").append(c2);
            }
        });
    }

    /**
     * 将 tableId fieldId 转换为业务名
     * 注意：如果不是模板会设置template的表别名
     */
    private void fillTableAndFieldNameForFilter(FilterDTO filter, int type) {
        Map<Long, String> nameMap;
        boolean isTemplate = Objects.equals(type, CommonConstants.TEMPLATE);
        if (isTemplate) {
            nameMap = filedIdToNameMap;
        } else {
            nameMap = filedIdToTemplateNameMap;
        }

        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterDTO : filters) {
                this.fillTableAndFieldNameForFilter(filterDTO, type);
            }
        }

        FilterDTO.FilterItemDTO filterItem = filter.getFilterItem();
        if (Objects.isNull(filterItem)) {
            return;
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.fillTableAndFieldNameForFilter(filterDTO, type);
                    }
                });
        Long tableId = filterItem.getTableId();
        if (Objects.isNull(tableId)) {
            log.warn("tableId is null: id={}", filterItem.getId());
            return;
        }

        boolean notExist = tableIdSet.add(tableId);
        if (notExist) {
            TableInfoManageDto detail = getTableInfoService().getDetail(tableId);
            if (Objects.isNull(detail)) {
                return;
            }

            List<TableFieldInfo> tableFieldInfos = detail.getTableFieldInfos();
            if (CollectionUtils.isNotEmpty(tableFieldInfos)) {
                for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
                    if (!nameMap.containsKey(tableFieldInfo.getId())) {
                        nameMap.put(tableFieldInfo.getId(), tableFieldInfo.getFieldName());
                    }
                }
            }
        }

        String fieldName = nameMap.get(filterItem.getFieldId());
        filterItem.setFieldName(fieldName);
        log.info("set: id={} name={}", filterItem.getFieldId(), fieldName);
        if (Objects.isNull(filterItem.getFieldName())) {
            try {
                CustomFieldInfo customFieldInfo = new CustomFieldInfo().selectById(filterItem.getFieldId());
                if (Objects.nonNull(customFieldInfo)) {
                    filterItem.setFieldName(customFieldInfo.getFieldName());
                }
            } catch (Exception e) {
                // 单元测试使用
                filterItem.setFieldName(String.valueOf(filterItem.getFieldId()));
                log.error("", e);
            }
        }

        if (!isTemplate) {
            filterItem.setTableAlias("template");
        } else {
            log.info("template={}", filterItem);
            // 为已经设置了别名的数据打补丁。。
            filterItem.setTableAlias(null);
        }
    }

    private void appendRangeSQL(FilterDTO filter, int type) {
        String whereSql = buildRangeSQL(filter, type);
        if (StringUtils.isNoneBlank(whereSql)) {
            rangeSqlBuilder.append(" WHERE ").append(whereSql);
        }
    }

    public String buildRangeSQL(FilterDTO filter, int type) {
        log.info("range: filter={} type={}", JsonUtils.format(filter), type);
        if (Objects.isNull(filter)) {
            return null;
        }

        this.fillTableAndFieldNameForFilter(filter, type);

        // TODO 多表，多字段 真实名,
        //  聚合指标名字传递
        try {
            Table table = new Table();
            table.setUniqueId(1L);
            table.setFactTable(true);
            Filter targetFilter = new Filter();
            ApplicationSqlUtil.convertToFilter(filter, targetFilter);
            final ClickHouse clickHouse = new ClickHouse(Collections.singletonList(table), targetFilter);
            return clickHouse.getWhereSql();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public String buildRangeByMySQL(FilterDTO filter, int type) {
        log.info("range: filter={} type={}", JsonUtils.format(filter), type);
        if (Objects.isNull(filter)) {
            return null;
        }

        this.fillTableAndFieldNameForFilter(filter, type);
        try {
            Table table = new Table();
            table.setUniqueId(1L);
            table.setFactTable(true);
            Filter targetFilter = new Filter();
            ApplicationSqlUtil.convertToFilter(filter, targetFilter);
            final MySql mySql = new MySql(Collections.singletonList(table), targetFilter);
            mySql.setHiddenQuote(true);
            return mySql.getWhereSql();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    private void checkRangeException(DataRangeInfoDto d, int type) throws ApplicationValidateException, TemplateValidateException {
        if ((d.getConditions() == null
                || ((d.getConditions() == CommonConstants.IN || d.getConditions() == CommonConstants.NOT_IN)
                && d.getIsAllSelected() != CommonConstants.ALL_SELECT
                && StringUtils.isBlank(d.getContent()))) && !d.getType().contains("Date")) {
            if (type == CommonConstants.TEMPLATE) {
                throw new TemplateValidateException();
            }
            if (type == CommonConstants.APPLICATION) {
                throw new ApplicationValidateException();
            }
        }
    }

    /**
     * 聚合指标 SQL 拼接
     * <p>
     * 1. 普通函数聚合指标
     * 2. 任意输入自定义指标 废弃
     * 3. 预设复杂模板指标
     */
    private void constructMetrics(List<MetricsInfoDto> metricsInfoDtos, int type, boolean createTemplate, boolean single) {
        boolean isTemplate = Objects.equals(CommonConstants.TEMPLATE, type);

        // 自定义公式指标 废弃
//        this.handleDiyCustomField(metricsInfoDtos, type, createTemplate, isTemplate);

        // 预设复杂模板指标 例如 单价
        this.handleCustomTemplateField(metricsInfoDtos, type, createTemplate, isTemplate);

        // 普通函数聚合指标
        this.handleFuncCustomField(metricsInfoDtos, type, createTemplate, single);
    }

    /**
     * SQL构造方式：
     * <p>
     * 内层模板创建出 别名字段：t_1_pp_xs_ps_sum 表名字段名聚合方式,
     * 外层申请引用字段 SUM(t_1_pp_xs_ps_sum) `申请时填的别名`
     * <p>
     * 指标存储：将内层和外层的自定义指标都存储入 custom_field_info
     */
    private void handleFuncCustomField(List<MetricsInfoDto> metricsInfoDtos, int type, boolean createTemplate, boolean single) {
        boolean isTemplate = Objects.equals(CommonConstants.TEMPLATE, type);
        boolean isApply = Objects.equals(CommonConstants.APPLICATION, type);
        for (MetricsInfoDto m : metricsInfoDtos) {
            TableInfoManageDto detail = this.getTableInfoService().getDetail(m.getTableId());
            if (Objects.equals(m.getComputeWay(), CommonConstants.ComputeWay.DIY)
                    || m.getComputeWay() > CommonConstants.ComputeWay.CUSTOM_START) {
                continue;
            }
            if (detail.getId() == null) {
                continue;
            }
            if (Objects.isNull(m.getColName())) {
                throw new RuntimeException("请选择具体的聚合指标字段");
            }
            String tn = detail.getTableNameDistributed();
            // FIXME: 申请时聚合模板中的聚合指标 ID源头不一样
            // NOW 没有字段id 直接拼接进SQL
            String fn = this.getFieldInfoService().getById(m.getColName()).getFieldName();
            String fullNameDot = tableAliasMap.get(tn) + "." + fn;
            String fullNameUnderScore = tableAliasMap.get(tn) + "_" + fn;
            String fullNameReal = visitedCols.contains(fullNameUnderScore) ? fullNameUnderScore : fullNameDot;
            StringBuilder metricSQL = new StringBuilder();
            CustomFieldInfo customFieldInfo = null;

            TableFieldInfo tmp = getFieldInfoService().getById(m.getColName());
            TableFieldInfo field = tmp != null ? tmp : new TableFieldInfo() {{
                Optional<CustomFieldInfo> customFieldInfo = getCustomMapper()
                        .selectCustomFields(templateUuid, CommonConstants.TEMPLATE)
                        .stream().filter(x -> x.getId().longValue() == m.getColName()).findFirst();
                customFieldInfo.ifPresent(x -> {
                    setFieldName(x.getFieldName());
                    setFieldType("String");
                    setTableId(x.getTableId());
                    m.setType("String");
                    fieldAliasMap.put(tn + "." + x.getFieldName(), x.getFieldName());
                });
            }};

            CustomFieldBuilderVO.CustomFieldBuilderVOBuilder builder = CustomFieldBuilderVO.builder()
                    .tableId(field.getTableId())
                    .fieldId(field.getId())
                    .type(type)
                    .aliasName(m.getAliasName())
                    .fullNameUnderScore(fullNameUnderScore)
                    .createTemplate(createTemplate);

            String realFnUnderScore = tableAliasMap.get(tn) + "_" + field.getFieldName();

            if (ObjectUtils.isNotNull(m.getIsConditions()) && m.getIsConditions() == CommonConstants.CONDITION) {
                Optional<CommonConstants.ComputeWayEnum> funcOpt = CommonConstants.ComputeWayEnum.getById(m.getComputeWay());
                if (!funcOpt.isPresent()) {
                    log.error("not support: compute way={}", m.getComputeWay());
                    continue;
                }
                CommonConstants.ComputeWayEnum wayEnum = funcOpt.get();
                String expression = wayEnum.buildExpression(fullNameReal, isApply || single, null);
                if (m.getComputeWay() == CommonConstants.ComputeWay.CUR) {
                    metricSQL.append(expression);
                } else if (m.getComputeWay() == CommonConstants.ComputeWay.MAX) {
                    metricSQL.append(expression);
                    builder.expression(expression);
                    builder.func(CommonConstants.ComputeWay.MAX_STR);
                    this.appendCustomFieldName(builder.build());

                } else if (m.getComputeWay() == CommonConstants.ComputeWay.MIN) {
                    metricSQL.append(expression);
                    builder.expression(expression);
                    builder.func(CommonConstants.ComputeWay.MIN_STR);
                    this.appendCustomFieldName(builder.build());

                } else if (m.getComputeWay() == CommonConstants.ComputeWay.SUM) {
                    metricSQL.append(expression);
                    builder.expression(expression);
                    builder.func(CommonConstants.ComputeWay.SUM_STR);
                    this.appendCustomFieldName(builder.build());

                } else if (m.getComputeWay() == CommonConstants.ComputeWay.AVG) {
                    metricSQL.append(expression);
                    builder.expression(expression);
                    builder.func(CommonConstants.ComputeWay.AVG_STR);
                    this.appendCustomFieldName(builder.build());

                } else if (m.getComputeWay() == CommonConstants.ComputeWay.COUNT) {
                    metricSQL.append(expression);
                    builder.expression(expression);
                    builder.func(CommonConstants.ComputeWay.COUNT_STR);
                    this.appendCustomFieldName(builder.build());

                } else if (m.getComputeWay() == CommonConstants.ComputeWay.COUNT_DISTINCT) {
                    metricSQL.append(expression);
                    if (isApply || isTemplate) {
                        colSqlBuilder.append(expression);
                        if (StringUtils.isNoneBlank(m.getAliasName())) {
                            colSqlBuilder.append(" `").append(m.getAliasName()).append("` ");
                        } else {
                            colSqlBuilder.append(realFnUnderScore).append("_count_distinct");
                        }

                        appendCommas(colSqlBuilder);

                        long uuid;
                        if (isApply) {
                            uuid = applicationUuid;
                        } else {
                            uuid = templateUuid;
                        }
                        customFieldInfo = buildCustomFieldInfo(fullNameUnderScore, m.getAliasName(),
                                CommonConstants.ComputeWay.COUNT_DISTINCT_STR, uuid,
                                field.getTableId(), field.getId(), type);
                    }

                    // 保存模板 或者 申请中新增的指标
                    if (isApply || (createTemplate && isTemplate)) {
                        this.upsertCustomField(customFieldInfo);
                    }
                }

                if (type == CommonConstants.TEMPLATE) {
                    groupByRemoveSet.add(fullNameDot);
                }

                if (type == CommonConstants.APPLICATION) {
                    groupByRemoveSet.add(fullNameUnderScore);
                }
            }

            if (Objects.equals(m.getIsConditions(), CommonConstants.CONDITION) && StringUtils.isNotBlank(m.getContent())) {
                this.appendHavingPart(m, metricSQL);
            }

        }
    }

    /**
     * 处理预设复杂模板指标 例如 单价
     */
    private void handleCustomTemplateField(List<MetricsInfoDto> metricsInfoDtos, int type, boolean createTemplate, boolean isTemplate) {
        Set<String> depFields = new HashSet<>();
        // 目前只有模板才可以配置，即该段逻辑只在模板类型下运行
        metricsInfoDtos.stream().filter(v -> v.getComputeWay() > CommonConstants.ComputeWay.CUSTOM_START).forEach(m -> {
            CustomFieldTemplate fieldTemplate = customFieldTemplateMapper.selectById(m.getComputeWay());
            StringBuilder metricSQL = new StringBuilder();

            // 拼接 保存依赖指标
            List<CustomFieldDepFieldVO> deps = fieldTemplate.parseTemplateDep();
            for (CustomFieldDepFieldVO dep : deps) {
                boolean add = depFields.add(dep.getField());
                if (!add) {
                    continue;
                }
                String templateAlias = dep.getAlias() + ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX;
                colSqlBuilder.append(dep.getOperator()).append("(t_1.").append(dep.getField()).append(") `").append(templateAlias).append("`");
                Integer computeWay = CommonConstants.ComputeWayEnum.getByFunc(dep.getOperator())
                        .orElseThrow(() -> new RuntimeException("不支持 " + dep.getOperator()));
                CustomFieldInfo customFieldInfo = this.buildCustomFieldInfo(templateAlias, computeWay, templateUuid, type);
                customFieldInfo.setTableId(fieldTemplate.getBaseTableId());

                if (Objects.equals(CommonConstants.APPLICATION, type) || createTemplate) {
                    this.upsertCustomField(customFieldInfo);
                }
                this.appendCommas(colSqlBuilder);
                visitedCols.add(dep.getField());
            }

//            metricSQL.append(fieldTemplate.getTemplate()).append(" `").append(name).append("_temp`");
//            colSqlBuilder.append(metricSQL);
//            this.appendCommas(colSqlBuilder);

            // 拼接 保存 计算公式指标
            if (Objects.equals(CommonConstants.APPLICATION, type) || (createTemplate && isTemplate)) {
                CustomFieldInfo customFieldInfo = this.buildCustomFieldInfo(m.getAliasName(), fieldTemplate.getId(), templateUuid, type);
                customFieldInfo.setTableId(fieldTemplate.getBaseTableId());
                this.upsertCustomField(customFieldInfo);
                log.info("add template custom field: {}", customFieldInfo);
            }

            String content = m.getExpressionContent();
            if (Objects.equals(m.getIsConditions(), CommonConstants.CONDITION) && StringUtils.isNotBlank(content)) {
                this.appendHavingPart(m, metricSQL);
            }
        });
    }

    @Deprecated
    private void handleDiyCustomField(List<MetricsInfoDto> metricsInfoDtos, int type, boolean createTemplate, boolean isTemplate) {
        metricsInfoDtos.stream().filter(v -> Objects.equals(v.getComputeWay(), CommonConstants.ComputeWay.DIY))
                .forEach(m -> {
                    if (!Objects.equals(m.getIsItself(), type)) {
                        return;
                    }

                    StringBuilder metricSQL = new StringBuilder();
                    String content = m.getExpressionContent();
                    metricSQL.append(content);

                    // 需要注意别名可能为中文
                    colSqlBuilder.append(content).append(" `").append(m.getAliasName()).append("_diy`");
                    this.appendCommas(colSqlBuilder);
                    CustomFieldInfo customFieldInfo = this.buildCustomFieldInfo(m.getAliasName(), m.getAliasName(),
                            CommonConstants.ComputeWay.DIY_STR, templateUuid, null, m.getColName(), type);

                    if (Objects.equals(CommonConstants.APPLICATION, type) ||
                            (createTemplate && isTemplate)) {
                        this.upsertCustomField(customFieldInfo);
                    }

                    log.info("add diy custom field: {}", customFieldInfo);

                    if (Objects.equals(m.getIsConditions(), CommonConstants.CONDITION) && StringUtils.isNotBlank(content)) {
                        this.appendHavingPart(m, metricSQL);
                    }
                });
    }

    private void appendHavingPart(MetricsInfoDto m, StringBuilder metricSQL) {
        Integer conditions = m.getConditions();
        if (Objects.isNull(conditions)) {
            return;
        }

        if (conditions == CommonConstants.IN || conditions == CommonConstants.NOT_IN) {
            boolean isReverse = m.getIsAllSelected() == CommonConstants.ALL_SELECT;
            String tempStr;
            if (conditions == CommonConstants.IN ^ isReverse) {
                tempStr = " GLOBAL IN (";
            } else {
                tempStr = " GLOBAL NOT IN (";
            }

            metricSQL.append(tempStr);

            // TODO 全选如何实现？
            if (ObjectUtils.isNotNull(m.getIsAllSelected())
                    && m.getIsAllSelected().equals(CommonConstants.ALL_SELECT)
                    && m.getContent().equals("")) {
                // 全选
                metricSQL.append("'" + Long.MIN_VALUE + "'");
            } else {
                // 范围选
                String content = "'" + Arrays.stream(m.getContent().split(",")).filter(StringUtils::isNotEmpty).collect(Collectors.joining("','")) + "'";
                metricSQL.append(content);
            }

            metricSQL.append(")");
        } else {
            if (conditions == CommonConstants.EQ) {
                metricSQL.append(" = ");
            } else if (conditions == CommonConstants.NE) {
                metricSQL.append(" != ");
            } else if (conditions == CommonConstants.GT) {
                metricSQL.append(" > ");
            } else if (conditions == CommonConstants.GE) {
                metricSQL.append(" >= ");
            } else if (conditions == CommonConstants.LT) {
                metricSQL.append(" < ");
            } else if (conditions == CommonConstants.LE) {
                metricSQL.append(" <= ");
            }
            metricSQL.append("'").append(m.getContent()).append("'");
        }

        if (havingBuilder.length() != 0) {
            havingBuilder.append(" AND ");
        }

        havingBuilder.append(metricSQL);
    }

    /**
     * 对单字段 聚合计算
     */
    private void appendCustomFieldName(CustomFieldBuilderVO builder) {
        String fullNameUnderScore = builder.getFullNameUnderScore();
        String func = builder.getFunc();
        String aliasName = builder.getAliasName();
        Integer type = builder.getType();
        Long fieldId = builder.getFieldId();
        Long tableId = builder.getTableId();
        String expression = builder.getExpression();
        Boolean createTemplate = builder.getCreateTemplate();

        CustomFieldInfo customFieldInfo = null;
        visitedCols.add(fullNameUnderScore);
        visitedCols.add(fullNameUnderScore + "_" + func.toLowerCase());
        boolean isTemplate = type == CommonConstants.TEMPLATE;
        if (isTemplate) {
            templateMetrics.add(fullNameUnderScore + "_" + func.toLowerCase());
            customFieldInfo = buildCustomFieldInfo(fullNameUnderScore, aliasName, func, templateUuid,
                    tableId, fieldId, CommonConstants.TEMPLATE);
        }
        boolean isApply = type == CommonConstants.APPLICATION;
        if (isApply) {
            customFieldInfo = buildCustomFieldInfo(fullNameUnderScore, aliasName, func, applicationUuid,
                    tableId, fieldId, CommonConstants.APPLICATION);
        }

        colSqlBuilder.append(expression);
        if (StringUtils.isNoneBlank(aliasName) && isApply) {
            colSqlBuilder.append(" `").append(aliasName).append("` ");
        } else {
            colSqlBuilder.append(fullNameUnderScore).append("_").append(func.toLowerCase());
        }

//        colSqlBuilder.append(expression).append(fullNameUnderScore).append("_").append(func.toLowerCase());

        if (Objects.equals(CommonConstants.APPLICATION, type) ||
                (createTemplate && Objects.equals(CommonConstants.TEMPLATE, type))) {
            this.upsertCustomField(customFieldInfo);
        }

        this.appendCommas(colSqlBuilder);
    }

    /**
     * 依据主键id（通过字段名和模板id计算MD5得到） 新增或更新自定义属性数据
     */
    private void upsertCustomField(CustomFieldInfo customFieldInfo) {
        if (Objects.isNull(customFieldInfo)) {
            return;
        }
        customFieldInfo.setHiddenForApply(false);
        log.warn("upsert customFieldInfo={}", customFieldInfo);
        this.getCustomMapper().insertOrUpdate(customFieldInfo);
    }

    // 隐藏已有指标，重新添加
    private void hideAllCustomField(Long sourceId) {
        log.warn("delete all custom field: sourceId={}", sourceId);
        if (Objects.isNull(sourceId)) {
            return;
        }

        CustomFieldInfo updateInfo = new CustomFieldInfo();
        updateInfo.setHiddenForApply(true);
        this.getCustomMapper().update(updateInfo,
                new QueryWrapper<CustomFieldInfo>().eq(ApplicationConst.FieldName.SOURCE_ID, sourceId));
    }

    private CustomFieldInfo buildCustomFieldInfo(String aliasName, Integer computeWay, Long sourceId, Integer source) {
        return new CustomFieldInfo() {{
            setId(IdUtils.getUUID(aliasName,
                    sourceId.toString(), String.valueOf(source)));
            setFieldAlias(aliasName);
            setRealName(aliasName);
            setComment(aliasName);
            setFieldName(aliasName);
            setSourceId(sourceId);
            setSource(source);
            setComputeWay(computeWay);

            // V1.3需求
            // 自定义聚合指标直接默认为数值类型
            // 字段派生的聚合指标 min/max 和原指标一致， count和count(distinct) 是数值类型
            setDataType(CkPgJavaDataType.Int32.toString());
        }};
    }


    private CustomFieldInfo buildCustomFieldInfo(String fullNameUnderScore, String aliasName, String func,
                                                 Long sourceId, Long tableId, Long fieldId, Integer source) {
        return new CustomFieldInfo() {{
            setId(IdUtils.getUUID(fullNameUnderScore + "_" + func.toLowerCase(),
                    sourceId.toString(), String.valueOf(source)));
            setFieldAlias(aliasName);
            setRealName(aliasName);
            setComment(aliasName);
            setTableId(tableId);
            setFieldName(fullNameUnderScore + "_" + func.toLowerCase());
            setSourceId(sourceId);
            setSource(source);
            Integer computeWay = CommonConstants.ComputeWayEnum.getByFunc(func).orElseThrow(() -> new RuntimeException("不支持 " + func));
            setComputeWay(computeWay);

            // V1.3需求
            // 自定义聚合指标直接默认为数值类型
            // 字段派生的聚合指标 min/max 和原指标一致， count和count(distinct) 是数值类型
            if (Objects.equals(func, CommonConstants.ComputeWay.DIY_STR)) {
                setDataType(CkPgJavaDataType.Int32.toString());
            } else {
                if (Objects.equals(func, CommonConstants.ComputeWay.COUNT_DISTINCT_STR)
                        || Objects.equals(func, CommonConstants.ComputeWay.COUNT_STR)) {
                    setDataType(CkPgJavaDataType.Int32.toString());
                } else if (Objects.equals(func, CommonConstants.ComputeWay.MAX_STR)
                        || Objects.equals(func, CommonConstants.ComputeWay.MIN_STR)
                        || Objects.equals(func, CommonConstants.ComputeWay.SUM_STR)
                        || Objects.equals(func, CommonConstants.ComputeWay.AVG_STR)
                ) {
                    TableFieldInfo tmp = getFieldInfoService().getById(fieldId);
                    String dataType = tmp.getDataType();
                    this.setDataType(dataType);
                }
            }
        }};
    }

    private ITemplateService getTemplateService() {
        return this.templateService;
    }

    private ITableInfoService getTableInfoService() {
        return tableInfoService;
    }

    private CustomFieldInfoMapper getCustomMapper() {
        return customMapper;
    }

    private ITableFieldInfoService getFieldInfoService() {
        return fieldInfoService;
    }

    private void resetSQL() {
        colSqlBuilder = new StringBuilder();
        metricsSqlBuilder = new StringBuilder();
        joinSqlBuilder = new StringBuilder();
        rangeSqlBuilder = new StringBuilder();
        havingBuilder = new StringBuilder();
        groupBySet.clear();
        groupByRemoveSet.clear();
    }

    private void appendCommas(StringBuilder stringBuilder) {
        if (stringBuilder.charAt(stringBuilder.length() - 1) != ',') {
            stringBuilder.append(",");
        }
    }

}
