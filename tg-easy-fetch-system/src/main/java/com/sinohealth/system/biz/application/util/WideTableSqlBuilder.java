package com.sinohealth.system.biz.application.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.bi.constant.SqlConstant;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.bi.enums.FunctionalOperatorEnum;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.dict.MetricsTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SpringContextUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.common.utils.uuid.IdUtils;
import com.sinohealth.system.biz.application.bo.CustomFieldBuilderVO;
import com.sinohealth.system.biz.application.constants.FieldType;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.PackTailFieldDto;
import com.sinohealth.system.biz.application.dto.SelectFieldDto;
import com.sinohealth.system.biz.application.service.impl.ApplicationTaskConfigServiceImpl;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.dao.PresetMetricsDefineDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.domain.PresetMetricsDefine;
import com.sinohealth.system.biz.dict.mapper.FieldDictMapper;
import com.sinohealth.system.domain.CustomFieldInfo;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.TgTemplatePackTailSetting;
import com.sinohealth.system.domain.ckpg.CkPgJavaDataType;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.dto.application.MetricsInfoDto;
import com.sinohealth.system.dto.application.RealName;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.mapper.CustomFieldInfoMapper;
import com.sinohealth.system.mapper.CustomFieldTemplateMapper;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITemplatePackTailSettingService;
import com.sinohealth.system.service.impl.DefaultSyncHelper;
import com.sinohealth.system.service.impl.TableFieldInfoServiceImpl;
import com.sinohealth.system.service.impl.TableInfoServiceImpl;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 宽表模式 构建资产表查询SQL
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-26 16:56
 */
@Slf4j
@Data
public class WideTableSqlBuilder {

    private static final Map<String, String> periodFuncMap = new HashMap<>();

    static {
        periodFuncMap.put(CommonConstants.YEAR,
                " toString(toYear(#)) period_str, parseDateTimeBestEffort(period_str) as period_new, '年度' as period_type ");
        periodFuncMap.put(CommonConstants.HFYEAR, " (toString(toYear(#)) || " +
                "multiIf(toMonth(#) <= '6', 'H1','H2')" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(#)) || " +
                "multiIf(toMonth(#) <= '6', '01','07')" +
                ")) as period_new, '半年度' as period_type ");
        periodFuncMap.put(CommonConstants.SEASON, " (toString(toYear(#)) || " +
                "multiIf(toQuarter(#) = '1', 'Q1'," +
                "toQuarter(#) = '2', 'Q2'," +
                "toQuarter(#) = '3', 'Q3'," +
                "'Q4')" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(#)) || " +
                "multiIf(toQuarter(#) = '1', '01'," +
                "toQuarter(#) = '2', '04'," +
                "toQuarter(#) = '3', '07'," +
                "'10'))) as period_new, '季度' as period_type ");
        periodFuncMap.put(CommonConstants.MONTH, " (toString(toYear(#)) || " +
                "multiIf(toMonth(#) <= '9', '0' || toString(toMonth(#))," +
                "toString(toMonth(#)))" +
                ") period_str, parseDateTimeBestEffort(period_str) as period_new, '月度' as period_type ");
        // V1.7 取消日的聚合场景
//        periodFuncMap.put(CommonConstants.DAYOFMONTH, " (toString(toYear(#)) || " +
//                "multiIf(toMonth(#) <= '9', '0' || toString(toMonth(#))," +
//                "toString(toMonth(#))) || multiIf(toDayOfMonth(#) <= '9', '0' || toString(toDayOfMonth(#)), toString(toDayOfMonth(#)))" +
//                ") period_str, parseDateTimeBestEffort(period_str) as period_new, '日' as period_type ");
    }

    List<MetricsInfoDto> allMetrics = new LinkedList<>();
    private TgApplicationInfo application;
    private TgTemplateInfo template;
    private long templateUuid;
    private long applicationUuid;
    private String applyPeriodFieldId = null;
    /**
     * dis name -> alias
     */
    private Map<String, String> tableAliasMap = new HashMap<>();
    private Map<String, String> fieldAliasMap = new HashMap<>();
    private ITableInfoService tableInfoService;
    private CustomFieldInfoMapper customMapper;
    private MetricsDictDAO metricsDictDAO;
    private PresetMetricsDefineDAO presetMetricsDefineDAO;
    private ITableFieldInfoService fieldInfoService;
    private CustomFieldTemplateMapper customFieldTemplateMapper;
    private FieldDictMapper fieldDictMapper;
    private ITemplatePackTailSettingService templatePackTailSettingService;
    private boolean cleanTempUselessField;
    private Set<String> templateMetrics = new HashSet<>();
    // 中间计算
    private Set<String> visitedCols = new HashSet<>();
    // 中间计算
    private Set<Long> tableIdSet = new HashSet<>();
    private Map<Long, String> filedIdToNameMap = new HashMap<>();

    private Map<Long, String> filedIdToTemplateNameMap = new HashMap<>();
    private int idx = 0;

    private List<String> subQuerySql = TgCollectionUtils.newArrayList();

    private TgTemplatePackTailSetting setting;

    public WideTableSqlBuilder() {
        this.tableInfoService = SpringContextUtils.getBean(TableInfoServiceImpl.class);
        this.customMapper = SpringContextUtils.getBean(CustomFieldInfoMapper.class);
        this.fieldInfoService = SpringContextUtils.getBean(TableFieldInfoServiceImpl.class);
        this.metricsDictDAO = SpringContextUtils.getBean(MetricsDictDAO.class);
        this.presetMetricsDefineDAO = SpringContextUtils.getBean(PresetMetricsDefineDAO.class);
        this.fieldDictMapper = SpringContextUtils.getBean(FieldDictMapper.class);
        this.templatePackTailSettingService = SpringContextUtils.getBean(ITemplatePackTailSettingService.class);
        this.cleanTempUselessField = false;
    }

    public WideTableSqlBuilder(Object cleanTempUselessField) {
        this();
        if (Objects.nonNull(cleanTempUselessField) && Objects.equals(cleanTempUselessField, 1)) {
            this.cleanTempUselessField = true;
        } else {
            this.cleanTempUselessField = false;
        }
    }

    public ITableInfoService getTableInfoService() {
        return tableInfoService;
    }

    public CustomFieldInfoMapper getCustomMapper() {
        return customMapper;
    }

    public ITableFieldInfoService getFieldInfoService() {
        return fieldInfoService;
    }

    private void preHandleApply(TgApplicationInfo info) {
        if (CollectionUtils.isEmpty(template.getApplicationPeriodField())) {
            info.setPeriodType(null);
            return;
        }

        List<ApplicationGranularityDto> granularity = info.getGranularity();
        for (ApplicationGranularityDto dto : granularity) {
            boolean isTime = Objects.equals(dto.getGranularity(), FieldGranularityEnum.time.name());
            if (!isTime) {
                continue;
            }
            if (CollectionUtils.isEmpty(dto.getSelectGranularity())) {
//                    throw new CustomException("未选择时间粒度");
                info.setPeriodType(null);
                return;
            }

            String type = dto.getSelectGranularity().get(0);
            info.setPeriodType(type);
        }
    }

    /**
     * 填充查询SQL和表别名映射 等信息
     */
    public boolean fillApplication(TgApplicationInfo applicationInfo) {
        if (Objects.isNull(applicationInfo)) {
            return false;
        }
        this.application = applicationInfo;
        this.template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(application.getTemplateId()));
        if (Objects.isNull(template)) {
            return false;
        }

        Long packTailId = applicationInfo.getPackTailId();
        if (Objects.nonNull(packTailId)) {
            this.setting = JsonBeanConverter.convert2Obj(templatePackTailSettingService.findById(packTailId));
        }

        this.preHandleApply(application);

        templateUuid = IdUtils.getUUID(CommonConstants.TEMPLATE + template.getCreateTime());
        applicationUuid = IdUtils.getUUID(CommonConstants.APPLICATION + applicationInfo.getCreateTime());

        Optional.ofNullable(template.getTableInfo()).ifPresent(this::prepareAliasMap);
        Optional.ofNullable(application.calcTableInfo()).ifPresent(this::prepareAliasMap);

        // TODO 调试单层
//        template.setSqlBuildMode(ApplicationConst.SQL_MODE_SINGLE);

        // 构造申请SQL 单层SQL
        if (Objects.equals(template.getSqlBuildMode(), ApplicationConst.SQL_MODE_SINGLE)) {
            String applySQL;
            SqlBuilderContext applyCtx = new SqlBuilderContext();
//            if (BooleanUtils.isTrue(this.template.getPackTail())) {
            if (Objects.nonNull(applicationInfo.getPackTailSwitch()) && applicationInfo.getPackTailSwitch() && Objects.nonNull(packTailId)) {
                Pair<String, String> sqlPair = this.createSinglePackTailApplySQL(applyCtx);
                application.setAsql(sqlPair.getKey());
                application.setTailSql(sqlPair.getValue());
            } else {
                applySQL = this.createSingleApplicationSQL(applyCtx);
                application.setAsql(applySQL);
            }
        } else {
            // 子查询模式
            SqlBuilderContext tempCtx = new SqlBuilderContext();
            String templateSQL = this.createTemplateSQL(tempCtx);

            SqlBuilderContext applyCtx = new SqlBuilderContext();
            String applicationSQL = this.createApplicationSQL(applyCtx, tempCtx, templateSQL);
            application.setAsql(applicationSQL);
        }

        log.info("\nASQL={}\n", application.getAsql());
        if (StringUtils.isNotBlank(application.getTailSql())) {
            log.info("\nSQL={}\n", application.getTailSql());
        }

        // TODO 删除这行，副作用
        JsonBeanConverter.convert2Obj(application);
        applicationInfo.setTableAliasMapping(tableAliasMap);
        JsonBeanConverter.convert2Json(application);
        return true;
    }

    private void prepareAliasMap(String tables) {
        for (String t : tables.split(",")) {
            String st = StringUtils.replaceLast(t, "_shard", "_local");
            String tn = StringUtils.replaceLast(t, "_local", "_shard");
            tableAliasMap.computeIfAbsent(tn, v -> "t_" + ++idx);
            fieldInfoService.getFieldsByTableName(st)
                    .forEach(f -> fieldAliasMap.put(st + "." + f, tableAliasMap.get(tn) + "_" + f));
        }
    }


    public String createTemplateSQL(SqlBuilderContext tempCtx) {
        StringBuilder result = new StringBuilder();

        Set<Long> selectMetricsId = application.getCustomMetrics().stream().filter(v -> BooleanUtils.isTrue(v.getSelect()))
                .map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toSet());
        List<CustomMetricsLabelDto> selectTemplateMetrics = template.getCustomMetrics().stream()
                .filter(v -> selectMetricsId.contains(v.getMetricsId()))
                .collect(Collectors.toList());
        // 模板层 预设复杂模板指标 例如 单价
        // 模板层 普通函数聚合指标
        this.constructTemplateMetrics(tempCtx, selectTemplateMetrics, false);

        this.constructSelectCol(tempCtx, template.getColsInfo(), allMetrics, CommonConstants.TEMPLATE, CommonConstants.TEMPLATE);
//        this.constructJoin(tempCtx, template.getJoinInfo(), CommonConstants.TEMPLATE, CommonConstants.TEMPLATE);

        String tableName = Optional.ofNullable(template).map(TgTemplateInfo::getBaseTableId)
                .map(getTableInfoService()::getDetail).map(TableInfoManageDto::getTableNameDistributed)
                .orElse("");

        tempCtx.getColSqlBuilder().removeIf(v -> tempCtx.getGroupByRemoveSet().stream().anyMatch(x -> v.startsWith(x + " ")));
        result.append("SELECT ").append(String.join(",", tempCtx.getColSqlBuilder()))
                .append(" FROM ").append(tableName).append(" ").append(tableAliasMap.get(tableName));

        tempCtx.getGroupBySet().removeAll(tempCtx.getGroupByRemoveSet());
        String groupBy = StringUtils.join(tempCtx.getGroupBySet(), ",");
        String whereSql = this.buildWhereSql(template.getDataRangeInfo(), CommonConstants.TEMPLATE);

        appendIfNotBlank(result, "", tempCtx.getJoinSqlBuilder());
        appendIfNotBlank(result, " WHERE ", whereSql);
        appendIfNotBlank(result, " GROUP BY ", groupBy);
        appendIfNotBlank(result, " HAVING ", tempCtx.getHavingBuilder());

        tempCtx.setTemplateWhere(whereSql);
        tempCtx.setTemplateTable(tableName);
        tempCtx.setTemplateAlias(tableAliasMap.get(tableName));
        return result.toString();
    }

    /**
     * 粒度设置中 选择的字段 模板层
     */
    private void constructTempSelect(SqlBuilderContext applyCtx, Map<Long, TableFieldInfo> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            return;
        }

        List<Long> tableIds = fieldMap.values().stream().map(TableFieldInfo::getTableId)
                .distinct().collect(Collectors.toList());

        List<TableInfo> tableInfos = getTableInfoService().getBaseMapper().selectBatchIds(tableIds);
        Map<Long, TableInfo> tableMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                v -> v, (front, current) -> current));

        for (Map.Entry<Long, TableFieldInfo> entry : fieldMap.entrySet()) {
            TableInfo tableInfo = tableMap.get(entry.getValue().getTableId());
            String tn = tableInfo.getTableNameDistributed();
            String fn;
            TableFieldInfo field = entry.getValue();
            try {
                fn = field.getFieldName();
                if (Objects.nonNull(field.getRelationColId())) {
                    filedIdToTemplateNameMap.put(field.getRelationColId(), tableAliasMap.get(tn) + "_" + fn);
                }

                applyCtx.getColSqlBuilder().add(tableAliasMap.get(tn) + "." + fn + " " + tableAliasMap.get(tn) + "_" + fn);

                applyCtx.getGroupBySet().add(tableAliasMap.get(tn) + "." + fn);
                fieldAliasMap.put(tn + "." + fn, tableAliasMap.get(tn) + "_" + fn);
                visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    /**
     * 粒度设置中 选择的字段
     */
    private void constructApplySelect(SqlBuilderContext applyCtx, Map<Long, TableFieldInfo> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            return;
        }

        List<Long> tableIds = fieldMap.values().stream().map(TableFieldInfo::getTableId)
                .distinct().collect(Collectors.toList());

        List<TableInfo> tableInfos = getTableInfoService().getBaseMapper().selectBatchIds(tableIds);
        Map<Long, TableInfo> tableMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                v -> v, (front, current) -> current));

        for (Map.Entry<Long, TableFieldInfo> entry : fieldMap.entrySet()) {
            TableInfo tableInfo = tableMap.get(entry.getValue().getTableId());
            String tn = tableInfo.getTableNameDistributed();
            String localTn = tableInfo.getTableName();
            String fn;
            String fid;
            TableFieldInfo field = entry.getValue();
            try {
                fn = field.getFieldName();
                fid = field.getId().toString();

                String afn = fieldAliasMap.get(localTn + "." + fn);
                applyCtx.getGroupBySet().add(tableAliasMap.get(tn) + "_" + fn);
                /* 如果不属于聚合时间字段 **/
                if (!fid.equals(application.getPeriodField())) {
                    applyCtx.getColSqlBuilder().add(afn);
                }
                visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


    /**
     * @param fieldType 选择字段范围的类型
     * @param aliasType 字段别名格式的类型以及SQL层次：申请（外层） 模板（内层）
     */
    private void constructSelectCol(SqlBuilderContext ctx,
                                    List<ColsInfoDto> colsInfoDtos,
                                    List<MetricsInfoDto> metricsInfoDtos,
                                    int fieldType, int aliasType) {
        boolean filedApply = fieldType == CommonConstants.APPLICATION;

        boolean aliasApply = aliasType == CommonConstants.APPLICATION;
        boolean aliasTemp = aliasType == CommonConstants.TEMPLATE;

        List<String> colSqlBuilder = ctx.getColSqlBuilder();
        Set<String> groupBySet = ctx.getGroupBySet();
        // 1. 普通原始字段，时间聚合字段
        for (ColsInfoDto c : colsInfoDtos) {
            // 申请模式下，模板过来的字段都走粒度（模板内的本表和关联表）
            if (filedApply && c.getIsItself() == CommonConstants.TEMPLATE) {
                continue;
            }
            TableInfoManageDto detail = getTableInfoService().getDetail(c.getTableId());
            String tn = detail.getTableNameDistributed();
            String localTn = detail.getTableName();

            Set<Long> depIds = metricsInfoDtos.stream()
                    .filter(v -> !Objects.equals(v.getComputeWay(), CommonConstants.ComputeWay.DIY))
                    .filter(v -> v.getComputeWay() < CommonConstants.ComputeWay.CUSTOM_START)
                    .map(MetricsInfoDto::getColName).filter(Objects::nonNull).collect(Collectors.toSet());
            for (Long id : c.getSelect()) {
                if (depIds.contains(id)) {
                    continue;
                }

                String fn;
                String fid;
                try {
                    TableFieldInfo field = getFieldInfoService().getById(id);
                    fn = field.getFieldName();
                    fid = field.getId().toString();

                    if (aliasApply && StringUtils.isNoneBlank(application.getPeriodField())
                            && Objects.equals(fn, "period")) {
                        log.warn("ignore apply period field");
                        return;
                    }
                    if (aliasTemp) {
                        if (Objects.nonNull(field.getRelationColId())) {
                            filedIdToTemplateNameMap.put(field.getRelationColId(), tableAliasMap.get(tn) + "_" + fn);
                        }
//
//                            colSqlBuilder.add(tableAliasMap.get(tn).append(".").append(fn)
//                                    .append(" ").append(tableAliasMap.get(tn)).append("_").append(fn));

                        colSqlBuilder.add(tableAliasMap.get(tn) + "." + fn + " " + tableAliasMap.get(tn) + "_" + fn);

                        groupBySet.add(tableAliasMap.get(tn) + "." + fn);
                        fieldAliasMap.put(tn + "." + fn, tableAliasMap.get(tn) + "_" + fn);
                        visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                    } else if (aliasApply) {
                        String afn = fieldAliasMap.get(localTn + "." + fn);
                        groupBySet.add(tableAliasMap.get(tn) + "_" + fn);
                        /* 如果不属于聚合时间字段 **/
                        if (!fid.equals(applyPeriodFieldId)) {
                            if (c.getIsItself() == CommonConstants.APPLICATION) {
//                                    colSqlBuilder.add(tableAliasMap.get(tn)).append(".").append(fn).append(" ").append(afn);
                                colSqlBuilder.add(tableAliasMap.get(tn) + "." + fn + " " + afn);
                            } else {
                                colSqlBuilder.add(afn);
                            }
                        }
                        visitedCols.add(tableAliasMap.get(tn) + "_" + fn);
                    }
                } catch (Exception e) {
                    log.error("", e);
                    fn = getCustomMapper().queryById(id).getFieldName();
                    if (!templateMetrics.contains(fn)) {
                        groupBySet.add(fn);
                        colSqlBuilder.add(fn);
                    }
                }
            }
        }
    }

    /**
     * 申请时追加模板使用的表
     */
    private Set<Long> depTableIds() {
        Set<Long> depTableIds = new HashSet<>();
        depTableIds.addAll(application.calcTableIds());
        depTableIds.addAll(template.calcTableIds());
        return depTableIds;
    }

    /**
     * 指标库 指标
     * <p>
     * 申请: 构建二次聚合指标, 预设指标
     */
    private void constructApplyMetrics(SqlBuilderContext applyCtx) {
        // 构造提数申请时：追加已选择的 提数模板中配置的聚合指标
        if (CollectionUtils.isEmpty(application.getCustomMetrics())) {
            return;
        }
        List<CustomMetricsLabelDto> metrics = application.calcSelectMetrics();
        if (CollectionUtils.isEmpty(metrics)) {
            return;
        }

        Map<Long, String> metricAliasMap = metrics.stream()
                .collect(Collectors.toMap(CustomMetricsLabelDto::getMetricsId, CustomMetricsLabelDto::getAlias,
                        (front, current) -> current));

        List<Long> metricsIds = metrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toList());

        List<MetricsDict> dicts = getMetricsDictDAO().listByIds(metricsIds);

        // 追加预设指标依赖的指标
        Map<Long, MetricsDict> presetMap = Lambda.buildMap(dicts, MetricsDict::getId, v -> v,
                v -> Objects.equals(v.getMetricsType(), MetricsTypeEnum.preset.name()));
        Map<Long, List<PresetMetricsDefine>> presetMapping = Lambda.queryMapIfExist(presetMap.keySet(),
                presetMetricsDefineDAO::queryByPresetMetricsId);
        Set<Long> depMetricsIdSet = presetMapping.values().stream().flatMap(Collection::stream)
                .map(PresetMetricsDefine::getMetricsId).collect(Collectors.toSet());
        Map<Long, MetricsDict> depMetricsMap = Lambda.queryMapIfExist(depMetricsIdSet, metricsDictDAO::listByIds, MetricsDict::getId);
        // 字段库 id
        Set<Long> fieldDictIds = dicts.stream()
                .filter(dict -> Objects.equals(dict.getMetricsType(), MetricsTypeEnum.formula.name()))
                .map(MetricsDict::getColName).collect(Collectors.toSet());
        fieldDictIds.addAll(depMetricsMap.values().stream().map(MetricsDict::getColName).collect(Collectors.toList()));

        Set<Long> depTables = this.depTableIds();
        List<TableFieldInfo> fieldInfos = getFieldInfoService().getBaseMapper().selectList(
                new QueryWrapper<TableFieldInfo>().lambda()
                        .in(TableFieldInfo::getTableId, depTables)
                        .in(TableFieldInfo::getRelationColId, fieldDictIds));
        Map<Long, TableFieldInfo> fieldMap = fieldInfos.stream()
                .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));
        Set<Long> tableIds = fieldInfos.stream().map(TableFieldInfo::getTableId).collect(Collectors.toSet());
        List<TableInfo> tableInfos = getTableInfoService().getBaseMapper().selectBatchIds(tableIds);
        Map<Long, String> tableNameMap = Lambda.buildMap(tableInfos, TableInfo::getId, TableInfo::getTableName);

        for (MetricsDict dict : dicts) {
            TableFieldInfo field = fieldMap.get(dict.getColName());
            String alias = metricAliasMap.get(dict.getId());
            String back = Optional.ofNullable(field).map(v -> v.getFieldName() + "_apply").orElse(StrUtil.randomAlpha(4) + "_apply");
            String aliasName = StringUtils.isNotBlank(alias) ? alias : " " + back;

            if (Objects.equals(dict.getMetricsType(), MetricsTypeEnum.preset.name())) {
                // 构造依赖指标的名字
                List<PresetMetricsDefine> depMetricsIds = presetMapping.get(dict.getId());
                Map<String, String> nameMap = depMetricsIds.stream().map(v -> depMetricsMap.get(v.getMetricsId()))
                        .collect(Collectors.toMap(v -> String.valueOf(v.getId()),
                                MetricsDict::buildApplyPresetMetricsFormula, (front, current) -> current));
                // 处理精度和 除数0 问题
                Optional<String> forOpt = ApplicationSqlUtil.buildFormula(dict.getFormula(), nameMap,
                        dict.getPrecisionNum(), dict.getDivisorMode());
                depMetricsIds.stream().map(v -> depMetricsMap.get(v.getMetricsId())).forEach(v -> {
                    TableFieldInfo depField = fieldMap.get(v.getColName());
                    String tableName = tableNameMap.get(depField.getTableId());
                    String applyName = fieldAliasMap.get(String.format("%s.%s", tableName, depField.getFieldName()));
                    applyCtx.getGroupByRemoveSet().add(applyName);
                });

                forOpt.ifPresent(v -> applyCtx.getColSqlBuilder().add(v + " `" + aliasName + "`"));
            } else if (Objects.equals(dict.getMetricsType(), MetricsTypeEnum.formula.name())) {
                if (Objects.isNull(field)) {
                    log.error("{} 指标 关联的字段库id{} 未关联到表:{}", dict.getName(), dict.getColName(), depTables);
                    throw new CustomException("请检查库表及模板配置：" + dict.getName() + " 指标依赖的字段库字段未关联到库表");
                }

                String metricsFormula = dict.buildApplyMetricsFormula();
                if (Objects.nonNull(metricsFormula)) {
                    String tableName = tableNameMap.get(field.getTableId());
                    String applyName = fieldAliasMap.get(String.format("%s.%s", tableName, field.getFieldName()));

                    String part = metricsFormula + " `" + ApplicationSqlUtil.trimMetricSuffix(aliasName) + "` ";
                    applyCtx.getColSqlBuilder().add(part);
                    applyCtx.getGroupByRemoveSet().add(applyName);
                }
            }
        }
    }

    /**
     * 单层: 构建二次聚合指标 预设指标
     *
     * @see this#constructApplyMetrics
     */
    private void constructSingleMetrics(SqlBuilderContext ctx, List<CustomMetricsLabelDto> metrics) {
        Map<Long, String> metricAliasMap = metrics.stream().collect(Collectors.toMap(CustomMetricsLabelDto::getMetricsId,
                CustomMetricsLabelDto::getAlias, (front, current) -> current));

        List<Long> metricsIds = metrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(metricsIds)) {
            return;
        }
        List<MetricsDict> dicts = getMetricsDictDAO().listByIds(metricsIds);

        // 追加预设指标依赖的指标
        Map<Long, MetricsDict> presetMap = Lambda.buildMap(dicts, MetricsDict::getId, v -> v,
                v -> Objects.equals(v.getMetricsType(), MetricsTypeEnum.preset.name()));
        Map<Long, List<PresetMetricsDefine>> presetMapping = Lambda.queryMapIfExist(presetMap.keySet(),
                presetMetricsDefineDAO::queryByPresetMetricsId);
        Set<Long> depMetricsIdSet = presetMapping.values().stream().flatMap(Collection::stream)
                .map(PresetMetricsDefine::getMetricsId).collect(Collectors.toSet());
        Map<Long, MetricsDict> depMetricsMap = Lambda.queryMapIfExist(depMetricsIdSet, metricsDictDAO::listByIds, MetricsDict::getId);
        // 字段库 id
        Set<Long> fieldDictIds = dicts.stream()
                .filter(dict -> Objects.equals(dict.getMetricsType(), MetricsTypeEnum.formula.name()))
                .map(MetricsDict::getColName).collect(Collectors.toSet());
        fieldDictIds.addAll(depMetricsMap.values().stream().map(MetricsDict::getColName).collect(Collectors.toList()));

        // 普通字段的指标
        List<TableFieldInfo> fieldInfos = getFieldInfoService().getBaseMapper().selectList(
                new QueryWrapper<TableFieldInfo>().lambda()
                        .in(TableFieldInfo::getTableId, depTableIds())
                        .in(TableFieldInfo::getRelationColId, fieldDictIds));
        // 指标id -> 真实字段
        Map<Long, TableFieldInfo> fieldMap = fieldInfos.stream()
                .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));

        Set<Long> tableIds = fieldInfos.stream().map(TableFieldInfo::getTableId).collect(Collectors.toSet());
        List<TableInfo> tableInfos = getTableInfoService().getBaseMapper().selectBatchIds(tableIds);
        Map<Long, String> tableNameMap = Lambda.buildMap(tableInfos, TableInfo::getId, TableInfo::getTableName);

        for (MetricsDict dict : dicts) {
            TableFieldInfo field = fieldMap.get(dict.getColName());
            String alias = metricAliasMap.get(dict.getId());

            String aliasName = StringUtils.isNotBlank(alias) ? alias : " " + field.getFieldName() + "_apply";

            if (!Objects.equals(dict.getMetricsType(), MetricsTypeEnum.preset.name())) {
                continue;
            }
            // 构造依赖指标的名字
            List<PresetMetricsDefine> depMetricsIds = presetMapping.get(dict.getId());
            Map<String, String> nameMap = depMetricsIds.stream().map(v -> depMetricsMap.get(v.getMetricsId()))
                    .collect(Collectors.toMap(v -> String.valueOf(v.getId()),
                            v -> {
                                TableFieldInfo depField = fieldMap.get(v.getColName());
                                if (Objects.isNull(depField)) {
                                    log.error("指标关联字段错误: id={} name={} fieldId={}", v.getId(), v.getName(), v.getColName());
                                    throw new CustomException("指标引用错误");
                                }
                                String tableName = tableNameMap.get(depField.getTableId());
                                String tabAlias = tableAliasMap.get(StringUtils.replaceLast(tableName, "_local", "_shard"));
                                return v.buildSingleTemplateMetricsFormula(String.format("%s.%s", tabAlias, depField.getFieldName()));
                            }, (front, current) -> current));

            // 处理精度
            Optional<String> forOpt = ApplicationSqlUtil.buildFormula(dict.getFormula(), nameMap,
                    dict.getPrecisionNum(), dict.getDivisorMode());
            depMetricsIds.stream().map(v -> depMetricsMap.get(v.getMetricsId())).forEach(v -> {

                TableFieldInfo depField = fieldMap.get(v.getColName());
                String tableName = tableNameMap.get(depField.getTableId());
                String applyName = fieldAliasMap.get(String.format("%s.%s", tableName, depField.getFieldName()));
                ctx.getGroupByRemoveSet().add(applyName);
            });

//            forOpt.ifPresent(v -> ctx.getColSqlBuilder().add(v + " `" + aliasName + "`"));
            forOpt.ifPresent(v -> ctx.addMetrics(dict.getId(), v + " `" + aliasName + "`"));
        }
    }


    private void constructJoin(SqlBuilderContext ctx, List<JoinInfoDto> joinInfoDtos, int type, int nameType) {
        StringBuilder joinSqlBuilder = ctx.getJoinSqlBuilder();
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
            if (nameType == CommonConstants.TEMPLATE) {
                joinSqlBuilder.append(" ON ").append(tableAliasMap.get(t1)).append(".").append(c1).append(" = ")
                        .append(tableAliasMap.get(t2)).append(".").append(c2);
            }
            if (nameType == CommonConstants.APPLICATION) {
                joinSqlBuilder.append(" ON ").append("template.").append(fieldAliasMap.get(localT1 + "." + c1)).append(" = ")
                        .append(tableAliasMap.get(t2)).append(".").append(c2);
            }
        });
    }


    /**
     * 申请 自定义指标
     * 聚合指标 SQL 拼接
     * <p>
     * 普通函数聚合指标
     */
    private void constructMetrics(SqlBuilderContext applyCtx, List<MetricsInfoDto> metricsInfoDtos, int type,
                                  boolean createTemplate, boolean single) {
        // 普通函数聚合指标
        this.handleFuncCustomField(applyCtx, metricsInfoDtos, type, createTemplate, single);
    }

    /**
     * 自定义指标，和指标库没关系
     * <p>
     * SQL构造方式：
     * <p>
     * 内层模板创建出 别名字段：t_1_pp_xs_ps_sum 表名字段名聚合方式,
     * 外层申请引用字段 SUM(t_1_pp_xs_ps_sum) `申请时填的别名`
     * <p>
     * 指标存储：将内层和外层的自定义指标都存储入 custom_field_info
     */
    private void handleFuncCustomField(SqlBuilderContext applyCtx, List<MetricsInfoDto> metricsInfoDtos, int type,
                                       boolean createTemplate, boolean single) {
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

            Optional<CommonConstants.ComputeWayEnum> funcOpt = CommonConstants.ComputeWayEnum.getById(m.getComputeWay());
            if (!funcOpt.isPresent()) {
                log.error("not support: compute way={}", m.getComputeWay());
                continue;
            }
            CommonConstants.ComputeWayEnum wayEnum = funcOpt.get();
            String expression = wayEnum.buildExpression(fullNameReal, isApply || single, null);
            if (m.getComputeWay() == CommonConstants.ComputeWay.MAX) {
                metricSQL.append(expression);
                builder.expression(expression);
                builder.func(CommonConstants.ComputeWay.MAX_STR);
                this.appendCustomFieldName(applyCtx, builder.build());

            } else if (m.getComputeWay() == CommonConstants.ComputeWay.MIN) {
                metricSQL.append(expression);
                builder.expression(expression);
                builder.func(CommonConstants.ComputeWay.MIN_STR);
                this.appendCustomFieldName(applyCtx, builder.build());

            } else if (m.getComputeWay() == CommonConstants.ComputeWay.SUM) {
                metricSQL.append(expression);
                builder.expression(expression);
                builder.func(CommonConstants.ComputeWay.SUM_STR);
                this.appendCustomFieldName(applyCtx, builder.build());

            } else if (m.getComputeWay() == CommonConstants.ComputeWay.AVG) {
                metricSQL.append(expression);
                builder.expression(expression);
                builder.func(CommonConstants.ComputeWay.AVG_STR);
                this.appendCustomFieldName(applyCtx, builder.build());

            } else if (m.getComputeWay() == CommonConstants.ComputeWay.COUNT) {
                metricSQL.append(expression);
                builder.expression(expression);
                builder.func(CommonConstants.ComputeWay.COUNT_STR);
                this.appendCustomFieldName(applyCtx, builder.build());

            } else if (m.getComputeWay() == CommonConstants.ComputeWay.COUNT_DISTINCT) {
                metricSQL.append(expression);
                if (StringUtils.isNoneBlank(m.getAliasName())) {
                    applyCtx.getColSqlBuilder().add(expression + " `" + m.getAliasName() + "` ");
                } else {
                    applyCtx.getColSqlBuilder().add(expression + " `" + realFnUnderScore + "_count_distinct` ");
                }

                long uuid;
                if (isApply) {
                    uuid = applicationUuid;
                } else {
                    uuid = templateUuid;
                }
                CustomFieldInfo customFieldInfo = buildCustomFieldInfo(fullNameUnderScore, m.getAliasName(),
                        CommonConstants.ComputeWay.COUNT_DISTINCT_STR, uuid,
                        field.getTableId(), field.getId(), type);

                // 保存模板 或者 申请中新增的指标
                if (isApply || (createTemplate && isTemplate)) {
                    this.upsertCustomField(customFieldInfo);
                }
            }

            if (type == CommonConstants.TEMPLATE) {
                applyCtx.getGroupByRemoveSet().add(fullNameDot);
            }

            if (type == CommonConstants.APPLICATION) {
                applyCtx.getGroupByRemoveSet().add(fullNameUnderScore);
            }

            if (StringUtils.isNotBlank(m.getContent())) {
                this.appendHavingPart(applyCtx, m, metricSQL);
            }
        }
    }

    /**
     * 对单字段 聚合计算
     */
    private void appendCustomFieldName(SqlBuilderContext ctx, CustomFieldBuilderVO builder) {
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

        if (StringUtils.isNoneBlank(aliasName) && isApply) {
            ctx.getColSqlBuilder().add(expression + " `" + aliasName + "` ");
        } else {
            ctx.getColSqlBuilder().add(expression + " `" + fullNameUnderScore + "_" + func.toLowerCase() + "` ");
        }

//        colSqlBuilder.append(expression).append(fullNameUnderScore).append("_").append(func.toLowerCase());

        if (Objects.equals(CommonConstants.APPLICATION, type) ||
                (createTemplate && Objects.equals(CommonConstants.TEMPLATE, type))) {
            this.upsertCustomField(customFieldInfo);
        }

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


    /**
     * 依据主键id（通过字段名和模板id计算MD5得到） 新增或更新自定义属性数据
     */
    private void upsertCustomField(CustomFieldInfo customFieldInfo) {
        if (Objects.isNull(customFieldInfo)) {
            return;
        }
        customFieldInfo.setHiddenForApply(false);
        log.warn("upsert customFieldInfo={}", JsonUtils.format(customFieldInfo));
        this.getCustomMapper().insertOrUpdate(customFieldInfo);
    }

    /**
     * 指标库的指标构建
     * <p>
     * SQL构造方式：
     * <p>
     * 内层模板创建出 别名字段：指标名,
     * 外层申请引用字段 SUM(指标名) `申请时填的别名`
     *
     * @see WideTableSqlBuilder#constructApplyMetrics 为申请二次聚合而使用
     */
    private void constructTemplateMetrics(SqlBuilderContext tempCtx, List<CustomMetricsLabelDto> metricsInfos, boolean single) {
        List<Long> metricsIds = metricsInfos.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(metricsIds)) {
            return;
        }
        List<MetricsDict> dicts = getMetricsDictDAO().listByIds(metricsIds);
        Map<Long, MetricsDict> metricsMap = Lambda.buildMap(dicts, MetricsDict::getId);

        // 追加预设指标依赖的基础指标
        Set<Long> presetIds = new HashSet<>();
        for (CustomMetricsLabelDto m : metricsInfos) {
            MetricsDict metricsDict = metricsMap.get(m.getMetricsId());
            if (Objects.equals(metricsDict.getMetricsType(), MetricsTypeEnum.preset.name())) {
                presetIds.add(m.getMetricsId());
            }
        }
        Map<Long, List<PresetMetricsDefine>> presetMapping = Lambda.queryMapIfExist(presetIds,
                presetMetricsDefineDAO::queryByPresetMetricsId);
        Set<Long> depMetricsIdSet = presetMapping.values().stream().flatMap(Collection::stream)
                .map(PresetMetricsDefine::getMetricsId).filter(v -> !metricsIds.contains(v)).collect(Collectors.toSet());
        List<MetricsDict> deps = Lambda.queryListIfExist(depMetricsIdSet, metricsDictDAO::listByIds);
        metricsMap.putAll(Lambda.buildMap(deps, MetricsDict::getId));
        presetMapping.values().stream().flatMap(Collection::stream)
                .map(v -> {
                    if (metricsIds.contains(v.getMetricsId())) {
                        return null;
                    }
                    CustomMetricsLabelDto dep = new CustomMetricsLabelDto();
                    dep.setMetricsId(v.getMetricsId());
                    return dep;
                }).filter(Objects::nonNull).forEach(metricsInfos::add);

        // 查询真实字段
        Set<Long> fieldDicts = metricsMap.values().stream()
                .filter(dict -> Objects.equals(dict.getMetricsType(), MetricsTypeEnum.formula.name()))
                .map(MetricsDict::getColName).collect(Collectors.toSet());

        List<TableFieldInfo> fieldInfos = getFieldInfoService().getBaseMapper().selectList(
                new QueryWrapper<TableFieldInfo>().lambda()
                        .in(TableFieldInfo::getTableId, template.calcTableIds())
                        .in(TableFieldInfo::getRelationColId, fieldDicts));
        Map<Long, TableFieldInfo> fieldMap = fieldInfos.stream()
                .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));
        Set<Long> tableIds = fieldInfos.stream().map(TableFieldInfo::getTableId).collect(Collectors.toSet());
        List<TableInfo> tableInfos = getTableInfoService().getBaseMapper().selectBatchIds(tableIds);

        Map<Long, TableInfo> tableMap = Lambda.buildMap(tableInfos, TableInfo::getId, v -> v);

        for (CustomMetricsLabelDto m : metricsInfos) {
            MetricsDict metricsDict = metricsMap.get(m.getMetricsId());
            if (!Objects.equals(metricsDict.getMetricsType(), MetricsTypeEnum.formula.name())) {
                continue;
            }
            Optional<TableFieldInfo> fieldOpt = Optional.ofNullable(fieldMap.get(metricsDict.getColName()));
            Optional<String> tnOpt = fieldOpt.map(v -> tableMap.get(v.getTableId())).map(TableInfo::getTableNameDistributed);
            if (!fieldOpt.isPresent() || !tnOpt.isPresent()) {
                throw new RuntimeException("指标没有映射到表单字段");
            }

            String tn = tnOpt.get();
            String fn = fieldOpt.get().getFieldName();

            String fullNameDot = tableAliasMap.get(tn) + "." + fn;
            String fullNameUnderScore = tableAliasMap.get(tn) + "_" + fn;
            String fullNameReal = visitedCols.contains(fullNameUnderScore) ? fullNameUnderScore : fullNameDot;
            StringBuilder metricSQL = new StringBuilder();

            TableFieldInfo field = fieldOpt.get();

            String realFnUnderScore = tableAliasMap.get(tn) + "_" + field.getFieldName();

            Optional<CommonConstants.ComputeWayEnum> funcOpt = CommonConstants.ComputeWayEnum.getById(metricsDict.getComputeWay());
            if (!funcOpt.isPresent()) {
                log.error("not support: compute way={}", metricsDict.getComputeWay());
                continue;
            }
            CommonConstants.ComputeWayEnum wayEnum = funcOpt.get();

            String expression = wayEnum.buildExpression(fullNameReal, single, metricsDict.getPrecisionNum());
            metricSQL.append(expression);
            String alias = StringUtils.firstNonEmpty(m.getAlias(),
                    single ? metricsDict.getName() : metricsDict.getName() + ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX,
                    realFnUnderScore + wayEnum.getFuncName());
//            tempCtx.getColSqlBuilder().add(expression + "`" + alias + "` ");
            tempCtx.addMetrics(m.getMetricsId(), expression + "`" + alias + "` ");
//            log.info("prec={} alias={}", metricsDict.getPrecisionNum(), alias);
            tempCtx.getGroupByRemoveSet().add(fullNameDot);

            if (CommonConstants.SUPPORT_HAVING.contains(m.getConditions()) && StringUtils.isNotBlank(m.getContent())) {
                this.appendHavingPart(tempCtx, m, metricSQL);
            }
        }
    }

    private void appendHavingPart(SqlBuilderContext ctx, MetricsInfoDto m, StringBuilder metricSQL) {
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

        if (ctx.getHavingBuilder().length() != 0) {
            ctx.getHavingBuilder().append(" AND ");
        }

        ctx.getHavingBuilder().append(metricSQL);
    }

    private void appendHavingPart(SqlBuilderContext ctx, CustomMetricsLabelDto m, StringBuilder metricSQL) {
        Integer conditions = m.getConditions();
        if (Objects.isNull(conditions)) {
            return;
        }

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

        if (ctx.getHavingBuilder().length() != 0) {
            ctx.getHavingBuilder().append(" AND ");
        }

        ctx.getHavingBuilder().append(metricSQL);
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
        if (Objects.isNull(filterItem.getUniqueId())) {
            filterItem.setUniqueId(1L);
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.fillTableAndFieldNameForFilter(filterDTO, type);
                    }
                });

        // 查询字段库关联的真实字段 填充字段名
//        if (isTemplate && Objects.isNull(filterItem.getTableId())) {
        if (isTemplate) {
            List<TableFieldInfo> relations = fieldInfoService.getBaseMapper().selectList(new QueryWrapper<TableFieldInfo>().lambda()
                    .eq(TableFieldInfo::getRelationColId, filterItem.getFieldId())
                    .in(TableFieldInfo::getTableId, template.calcTableIds())
            );

            // 字段库id
            Map<Long, String> fieldNameMap = relations.stream()
                    .collect(Collectors.toMap(TableFieldInfo::getRelationColId, TableFieldInfo::getFieldName, (front, current) -> front));
            nameMap.putAll(fieldNameMap);
        }

        String fieldName = nameMap.get(filterItem.getFieldId());
        if (StringUtils.isBlank(fieldName)) {
            log.error("no name: id={} name={}", filterItem.getFieldId(), fieldName);
        } else {
//            log.info("set: id={} name={}", filterItem.getFieldId(), fieldName);
            filterItem.setFieldName(fieldName);
        }

//        if (Objects.isNull(filterItem.getFieldName())) {
//            // TODO 指标字段
//            try {
//                CustomFieldInfo customFieldInfo = new CustomFieldInfo().selectById(filterItem.getFieldId());
//                if (Objects.nonNull(customFieldInfo)) {
//                    filterItem.setFieldName(customFieldInfo.getFieldName());
//                }
//            } catch (Exception e) {
//                // 单元测试使用
//                filterItem.setFieldName(String.valueOf(filterItem.getFieldId()));
//                log.error("", e);
//            }
//        }

        if (!isTemplate) {
            filterItem.setTableAlias("template");
        } else {
//            log.info("template={}", filterItem);
            // 为已经设置了别名的数据打补丁。。
            filterItem.setTableAlias(null);

            if (Objects.nonNull(filterItem.getTableId())) {
                TableInfo tableInfo = tableInfoService.getBaseMapper().selectById(filterItem.getTableId());
                filterItem.setTableAlias(tableAliasMap.get(tableInfo.getTableNameDistributed()));
            }
        }
    }

    public String buildWhereSql(FilterDTO filter, int type) {
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
            final ClickHouse clickHouse = new ClickHouse(Collections.singletonList(table), targetFilter);
            return clickHouse.getWhereSql();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 追加需求: 时间聚合功能
     * 根据申请中 period_field 和 period_type
     * 在出数的 SQL 中的 col 和 group by 中追加时间聚合字段
     */
    private void constructApplyPeriodCol(SqlBuilderContext ctx, List<ColsInfoDto> colsInfos, boolean single) {
        for (ColsInfoDto c : colsInfos) {
            if (Objects.nonNull(application) && StringUtils.isNoneBlank(application.getPeriodType())) {
                TableInfoManageDto detail = getTableInfoService().getDetail(c.getTableId());
                String tn = detail.getTableNameDistributed();

                StringBuilder temp = new StringBuilder();

                List<Long> applicationPeriodField = template.getApplicationPeriodField();
                Long id = applicationPeriodField.get(0);
                TableFieldInfo field = getFieldInfoService().getBaseMapper()
                        .selectOne(new QueryWrapper<TableFieldInfo>().lambda()
                                .eq(TableFieldInfo::getTableId, c.getTableId())
                                .eq(TableFieldInfo::getRelationColId, id));
                if (Objects.isNull(field)) {
                    continue;
                }

                CkPgJavaDataType ckType = CkPgJavaDataType.resolveDefaultArrayDataType(field.getDataType());
                if (!ckType.equals(CkPgJavaDataType.Date)
                        && !ckType.name().startsWith(CkPgJavaDataType.DateTime.name())) {
                    continue;
                }

                String fn = field.getFieldName();
                applyPeriodFieldId = field.getId().toString();

                String applyPeriod;
                if (single) {
                    applyPeriod = tableAliasMap.get(tn) + "." + fn;
                } else {
                    applyPeriod = tableAliasMap.get(tn) + "_" + fn;
                }

                temp.append(applyPeriod);
                ctx.setApplyPeriod(applyPeriod);

                if (StringUtils.isNotBlank(application.getPeriodType())) {
                    this.appendPeriodFields(application.getPeriodType(), temp).ifPresent(ctx.getColSqlBuilder()::add);
                    ctx.getGroupBySet().add(ApplicationConst.PeriodField.PERIOD_NEW);
                    ctx.getGroupBySet().add(ApplicationConst.PeriodField.PERIOD_STR);
                    ctx.getGroupBySet().add(ApplicationConst.PeriodField.PERIOD_TYPE);
                    ctx.getGroupByRemoveSet().add(applyPeriod);
                    visitedCols.add(applyPeriod);
                }
            }
        }
    }

    /**
     * SQL追加 period_type period_str period_new
     */
    private Optional<String> appendPeriodFields(String type, StringBuilder fieldName) {
        return Optional.ofNullable(periodFuncMap.get(type)).map(v -> this.buildPeriodSQL(fieldName, v));
    }

    private String buildPeriodSQL(StringBuilder fieldName, String template) {
        String underLineTemp = fieldName.toString().replace(".", "_");
        if (!visitedCols.contains(underLineTemp)) {
            return template.replace("#", fieldName.toString());
        } else {
            return template.replace("#", underLineTemp);
        }
    }

    private void clearApplicationRecordFromTemplate() {
        application.getJoinInfo().removeIf(x -> x.getIsItself() == null || x.getIsItself() == CommonConstants.TEMPLATE);
        application.getMetricsInfo().removeIf(x -> x.getIsItself() == null || x.getIsItself() == CommonConstants.TEMPLATE);
    }

    /**
     * @see DefaultSyncHelper#buildCkLocalSqlByTmpTable 当前方法创建的查询SQL会影响到此处的SQL解析
     */
    public String createApplicationSQL(SqlBuilderContext applyCtx, SqlBuilderContext tempCtx, String lastTemp) {
        allMetrics.addAll(application.getMetricsInfo());

        this.clearApplicationRecordFromTemplate();

        this.constructApplyPeriodCol(applyCtx, application.getColsInfo(), false);

        SelectFieldContext selectCtx = buildApplySelectFieldContext();
        Map<Long, TableFieldInfo> selectFieldMap = selectCtx.getSelectFieldMap();
        List<TableFieldInfo> fieldInfos = selectCtx.getFieldInfos();
        List<Long> metricsQuoteIds = selectCtx.getMetricsQuoteIds();

        // 粒度字段本表 选择的字段
        this.constructApplySelect(applyCtx, selectFieldMap);

        // JOIN关联表 选择的字段
        this.constructSelectCol(applyCtx, application.getColsInfo(), allMetrics,
                CommonConstants.APPLICATION, CommonConstants.APPLICATION);

        this.constructApplyMetrics(applyCtx);

        if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
            Map<Long, TableFieldInfo> metricsFieldMap = fieldInfos.stream()
                    .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                    .filter(v -> metricsQuoteIds.contains(v.getRelationColId()))
                    .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));
            for (MetricsInfoDto metricsInfoDto : application.getMetricsInfo()) {
                TableFieldInfo field = metricsFieldMap.get(metricsInfoDto.getColName());
                metricsInfoDto.setTableId(field.getTableId());
                metricsInfoDto.setColName(field.getId());
            }
        }
        this.constructMetrics(applyCtx, application.getMetricsInfo(), CommonConstants.APPLICATION,
                false, false);

//        this.constructJoin(applyCtx, application.getJoinInfo(), CommonConstants.APPLICATION, CommonConstants.APPLICATION);
        List<String> conditions = application.getGranularity().stream()
                .map(ApplicationGranularityDto::getFilter).filter(Objects::nonNull)
                .map(v -> this.buildWhereSql(v, CommonConstants.APPLICATION))
                .collect(Collectors.toList());
//        String zoneNameWhere = this.buildZoneNameWhere(selectFieldMap);
//        if (Objects.nonNull(zoneNameWhere)) {
//            conditions.add(zoneNameWhere);
//        }
        String applyWhere = conditions.stream().map(v -> "(" + v + ")").collect(Collectors.joining(" AND "));

        List<String> colSqlBuilder = applyCtx.getColSqlBuilder();
        Set<String> groupByRemoveSet = applyCtx.getGroupByRemoveSet();

        // 指标不应进入select及group子句 维度才需要
        colSqlBuilder.removeIf(v -> groupByRemoveSet.stream().anyMatch(v::equals));

        this.cleanTemplateUselessField(applyCtx, tempCtx, applyWhere);
        String templateSql = tempCtx.buildTempSQL();
        log.info("origin: {} \n new: {}", lastTemp, templateSql);

        return applyCtx.buildApplySQL(templateSql, applyWhere);
    }

    private void appendIfNotBlank(StringBuilder result, String prefix, CharSequence segment) {
        if (StringUtils.isNotBlank(segment)) {
            result.append(prefix).append(segment);
        }
    }

    /**
     * 选择的市场粒度 拼进Where
     * <p>
     * 等于，且值中包含|，会处理in 例如 a|b -> in('a', 'b')
     */
    private String buildZoneNameWhere(Map<Long, TableFieldInfo> selectFieldMap) {
        Map<String, ApplicationGranularityDto> granularityMap = Lambda.buildMap(application.getGranularity(), ApplicationGranularityDto::getGranularity);
        ApplicationGranularityDto dto = granularityMap.get(FieldGranularityEnum.area.name());
        if (Objects.isNull(dto)) {
            return null;
        }

        String values = String.join(SqlConstant.IN_OR, dto.getSelectGranularity());
        FilterDTO filter = new FilterDTO();
        FilterDTO.FilterItemDTO item = new FilterDTO.FilterItemDTO();
        Optional<Long> zoneNameOpt = dto.getFields().stream().map(v -> selectFieldMap.get(v.getFieldId())).filter(Objects::nonNull)
                .filter(v -> Objects.equals(v.getFieldName(), "zone_name")).map(TableFieldInfo::getRelationColId).findFirst();
        if (!zoneNameOpt.isPresent()) {
            return null;
        }

        item.setTableId(application.getBaseTableId());
        item.setFieldId(zoneNameOpt.get());
        item.setValue(values);
        item.setFunctionalOperator(FunctionalOperatorEnum.EQUAL_TO.getType());
        filter.setFilterItem(item);
        String sql = this.buildWhereSql(filter, CommonConstants.APPLICATION);
//        log.warn("area: sql={}", sql);
        return sql;
    }

    /**
     * 隐藏模板内使用到但是申请没使用的字段
     * 没有 select
     * 没有 where
     * 没有 group by
     */
    private void cleanTemplateUselessField(SqlBuilderContext applyCtx, SqlBuilderContext tempCtx, String applyWhere) {
        // 自选维度才会需要优化
        if (!Objects.equals(template.getColAttr(), 1L) || !cleanTempUselessField) {
            return;
        }
        // 申请层 自定义指标 不处理
        // TODO 应该要按依赖指标不剔除，做精细控制
        if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
            return;
        }
        // 判断申请层 是否包含 模板层的别名字段
        List<String> cols = tempCtx.getColSqlBuilder();
        Set<String> rmTempCols = cols.stream()
                .filter(v -> v.contains(" ") && !v.contains("`"))
                .filter(i -> {
                    String[] pair = i.split(" ");
                    String alias = pair[1];
                    return !applyCtx.getGroupBySet().contains(alias)
                            && !applyCtx.getColSqlBuilder().contains(alias)
                            && (StringUtils.isNotBlank(applyCtx.getApplyPeriod()) && !Objects.equals(applyCtx.getApplyPeriod(), alias))
                            && !StringUtils.contains(applyWhere, alias)
                            ;
                })
                .collect(Collectors.toSet());
        Set<String> fields = rmTempCols.stream().map(v -> {
            String[] pair = v.split(" ");
            return pair[0];
        }).collect(Collectors.toSet());

        log.warn("rm template field: {}", String.join(",", rmTempCols));
        tempCtx.getColSqlBuilder().removeAll(rmTempCols);
        tempCtx.getGroupBySet().removeAll(fields);
    }

    /**
     * 单层 下 长尾SQL处理
     *
     * @see ApplicationTaskConfigServiceImpl#fillGranularity
     */
    public Pair<String, String> createSinglePackTailApplySQL(SqlBuilderContext ctx) {
        if (Objects.isNull(this.setting)) {
            throw new CustomException("无对应打包配置信息");
        }
        SingleLayerCtx singleLayerCtx = this.buildSingleLayerCtx(ctx);
        // 追加长尾过滤SQL
        String tailWhere = this.buildWhereSql(setting.getTailFilter(), CommonConstants.TEMPLATE);

        String finalWhere = Stream.of(singleLayerCtx.templateWhere, singleLayerCtx.applyWhere,
                        ApplicationSqlUtil.reverseSql(tailWhere))
                .filter(StringUtils::isNotBlank).map(v -> "(" + v + ")")
                .collect(Collectors.joining(" AND "));

        appendIfNotBlank(singleLayerCtx.result, " WHERE ", finalWhere);
        appendIfNotBlank(singleLayerCtx.result, " GROUP BY ", singleLayerCtx.groupBy);
        appendIfNotBlank(singleLayerCtx.result, " HAVING ", ctx.getHavingBuilder());

        // 处理长尾部分SQL
        List<String> tailCols = this.buildTailCols(ctx);
        StringBuilder tail = new StringBuilder().append("SELECT ")
                .append(String.join(",", tailCols))
                .append(" FROM ").append(singleLayerCtx.tableName)
                .append(" ").append(tableAliasMap.get(singleLayerCtx.tableName));

        appendIfNotBlank(tail, "", ctx.getJoinSqlBuilder());

        String finalTailWhere = Stream.of(singleLayerCtx.templateWhere, singleLayerCtx.applyWhere, tailWhere)
                .filter(StringUtils::isNotBlank).map(v -> "(" + v + ")")
                .collect(Collectors.joining(" AND "));
        appendIfNotBlank(tail, " WHERE ", finalTailWhere);

        String newGroupBy = String.join(",", ctx.getGroupBySet());
        appendIfNotBlank(tail, " GROUP BY ", newGroupBy);
        appendIfNotBlank(tail, " HAVING ", ctx.getHavingBuilder());
        return Pair.of(singleLayerCtx.result.toString(), tail.toString());
    }

    /**
     * 替换字段
     */
    private List<String> buildTailCols(SqlBuilderContext ctx) {
        List<String> colSqlBuilder = ctx.getColSqlBuilder();
        Map<Long, String> tempMetrics = ctx.getTempMetrics();

//        List<PackTailFieldDto> tailFields = template.getTailFields();
        List<PackTailFieldDto> tailFields = setting.getTailFields();
        Map<Integer, List<PackTailFieldDto>> tailTypeMap = tailFields.stream()
                .collect(Collectors.groupingBy(PackTailFieldDto::getFieldType));

        // 填入维度
        List<PackTailFieldDto> dims = tailTypeMap.get(FieldType.DIMENSIONS);

        List<Long> fieldIds = Lambda.buildList(dims, PackTailFieldDto::getFieldId);
        Map<Long, FieldDict> fieldDictMap = Lambda.queryMapIfExist(fieldIds, fieldDictMapper::selectBatchIds, FieldDict::getId);
        Map<String, PackTailFieldDto> tailFieldMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(dims)) {
            // 构造出 字段库字段 最后构造出的select字段名
            List<ColsInfoDto> colsInfo = application.getColsInfo();
            for (ColsInfoDto col : colsInfo) {
                String table = col.getTableName().replace(CkTableSuffixTable.LOCAL, CkTableSuffixTable.SHARD);
                List<RealName> realName = col.getRealName();
                Map<Long, RealName> colMap = Lambda.buildMap(realName, RealName::getRelationColId);

                for (PackTailFieldDto tailField : dims) {
                    RealName name = colMap.get(tailField.getFieldId());
                    if (Objects.nonNull(name)) {
                        String alias = tableAliasMap.get(table) + "_" + name.getFieldName();
                        tailFieldMap.put(alias, tailField);
                        ctx.getGroupBySet().remove(tableAliasMap.get(table) + "." + name.getFieldName());
                    }
                }
            }
        }

        // 填入指标
        List<PackTailFieldDto> metrics = tailTypeMap.get(FieldType.METRIC);
        if (CollectionUtils.isNotEmpty(metrics)) {
            for (PackTailFieldDto metric : metrics) {
                String key = tempMetrics.get(metric.getFieldId());
                if (StringUtils.isNotBlank(key)) {
                    tailFieldMap.put(key, metric);
                }
            }
        }

        List<String> tailCols = new ArrayList<>();
        for (String field : colSqlBuilder) {
            boolean replace = false;
            for (Map.Entry<String, PackTailFieldDto> entry : tailFieldMap.entrySet()) {
                if (field.endsWith(entry.getKey())) {
                    replace = true;
                    PackTailFieldDto dto = entry.getValue();
                    // 判断字段类型
                    boolean number = Optional.ofNullable(fieldDictMap.get(dto.getFieldId())).map(FieldDict::getDataType)
                            .map(DataDictDataTypeEnum::isDecimal).orElse(false);

                    if (BooleanUtils.isTrue(dto.getMarkNull())) {
                        tailCols.add("null");
                    } else if (Objects.equals(dto.getFieldType(), FieldType.METRIC)) {
                        // TODO 用到了AVG的指标 类型是Float，所以不能做类型转换
                        // 风险点：如果指标是AVG且转为Decimal，同样会报错
                        if (field.contains("AVG") && !field.contains("toDecimal")) {
                            tailCols.add(dto.getVal());
                        } else {
                            tailCols.add(String.format("toDecimal64(%s, 8)", dto.getVal()));
                        }
                    } else if (Objects.equals(dto.getFieldType(), FieldType.DIMENSIONS)) {
                        if (number) {
                            tailCols.add(dto.getVal());
                        } else {
                            tailCols.add("'" + dto.getVal() + "'");
                        }
                    }
                    break;
                }
            }
            if (!replace) {
                tailCols.add(field);
            }
        }
        return tailCols;
    }

    public String createSingleApplicationSQL(SqlBuilderContext ctx) {
        SingleLayerCtx singleLayerCtx = buildSingleLayerCtx(ctx);
        String finalWhere = Stream.of(singleLayerCtx.templateWhere, singleLayerCtx.applyWhere)
                .filter(StringUtils::isNotBlank).map(v -> "(" + v + ")")
                .collect(Collectors.joining(" AND "));

        appendIfNotBlank(singleLayerCtx.result, " WHERE ", finalWhere);
        appendIfNotBlank(singleLayerCtx.result, " GROUP BY ", singleLayerCtx.groupBy);
        appendIfNotBlank(singleLayerCtx.result, " HAVING ", ctx.getHavingBuilder());

        return singleLayerCtx.result.toString();
    }

    private SingleLayerCtx buildSingleLayerCtx(SqlBuilderContext ctx) {
        StringBuilder finalSQL = new StringBuilder();

        Map<Long, String> selectMetricsMap = application.getCustomMetrics().stream()
                .filter(v -> BooleanUtils.isTrue(v.getSelect()))
                .collect(Collectors.toMap(CustomMetricsLabelDto::getMetricsId, CustomMetricsLabelDto::getAlias, (front, current) -> current));

        List<CustomMetricsLabelDto> selectTemplateMetrics = template.getCustomMetrics().stream()
                .filter(v -> selectMetricsMap.containsKey(v.getMetricsId()))
                .peek(v -> v.setAlias(selectMetricsMap.get(v.getMetricsId())))
                .collect(Collectors.toList());

        this.constructApplyPeriodCol(ctx, application.getColsInfo(), true);

        this.constructSingleMetrics(ctx, selectTemplateMetrics);

        // 模板层 预设复杂模板指标 例如 单价
        // 模板层 普通函数聚合指标
        this.constructTemplateMetrics(ctx, selectTemplateMetrics, true);

        SelectFieldContext selectCtx = this.buildApplySelectFieldContext();
        Map<Long, TableFieldInfo> selectFieldMap = selectCtx.getSelectFieldMap();
        List<TableFieldInfo> fieldInfos = selectCtx.getFieldInfos();
        List<Long> metricsQuoteIds = selectCtx.getMetricsQuoteIds();


        if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
            Map<Long, TableFieldInfo> metricsFieldMap = fieldInfos.stream()
                    .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                    .filter(v -> metricsQuoteIds.contains(v.getRelationColId()))
                    .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));
            for (MetricsInfoDto metricsInfoDto : application.getMetricsInfo()) {
                TableFieldInfo field = metricsFieldMap.get(metricsInfoDto.getColName());
                metricsInfoDto.setTableId(field.getTableId());
                metricsInfoDto.setColName(field.getId());
            }
        }
        this.constructMetrics(ctx, application.getMetricsInfo(), CommonConstants.APPLICATION,
                false, true);

        // 粒度字段本表 选择的字段
        this.constructTempSelect(ctx, selectFieldMap);

        this.constructSelectCol(ctx, application.getColsInfo(), allMetrics,
                CommonConstants.APPLICATION, CommonConstants.TEMPLATE);

//        this.constructJoin(ctx, template.getJoinInfo(), CommonConstants.TEMPLATE, CommonConstants.TEMPLATE);
//        this.constructJoin(ctx, application.getJoinInfo(), CommonConstants.APPLICATION, CommonConstants.TEMPLATE);

        String tableName = Optional.ofNullable(template).map(TgTemplateInfo::getBaseTableId)
                .map(getTableInfoService()::getDetail).map(TableInfoManageDto::getTableNameDistributed)
                .orElse("");

        List<String> colSqlBuilder = ctx.getColSqlBuilder();
        Set<String> groupByRemoveSet = ctx.getGroupByRemoveSet();
        Set<String> groupBySet = ctx.getGroupBySet();

        log.info("Remove={}", String.join(",", groupByRemoveSet));
        colSqlBuilder.removeIf(v -> groupByRemoveSet.stream().map(x -> x + " ").anyMatch(v::startsWith));
        finalSQL.append("SELECT ").append(String.join(",", colSqlBuilder))
                .append(" FROM ").append(tableName)
                .append(" ").append(tableAliasMap.get(tableName));

        groupBySet.removeAll(groupByRemoveSet);
        String groupBy = StringUtils.join(groupBySet, ",");
        appendIfNotBlank(finalSQL, "", ctx.getJoinSqlBuilder());

        String applyWhere = application.getGranularity().stream()
                .map(ApplicationGranularityDto::getFilter).filter(Objects::nonNull)
                .map(v -> this.buildWhereSql(v, CommonConstants.TEMPLATE))
                .map(v -> "(" + v + ")")
                .collect(Collectors.joining(" AND "));
        String templateWhere = this.buildWhereSql(template.getDataRangeInfo(), CommonConstants.TEMPLATE);

        return new SingleLayerCtx(finalSQL, tableName, groupBy, applyWhere, templateWhere);
    }

    /**
     * 构建已选择字段 上下文
     */
    private SelectFieldContext buildApplySelectFieldContext() {
        // 构造 SELECT 字段
        List<SelectFieldDto> fields = application.getGranularity().stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getFields()))
                .flatMap(v -> v.getFields().stream())
                .collect(Collectors.toList());
        List<Long> fieldDicts = fields.stream().map(SelectFieldDto::getFieldId).collect(Collectors.toList());
        List<Long> metricsQuoteIds;
        if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
            metricsQuoteIds = application.getMetricsInfo().stream().map(MetricsInfoDto::getColName)
                    .collect(Collectors.toList());
            fieldDicts.addAll(metricsQuoteIds);
        } else {
            metricsQuoteIds = Collections.emptyList();
        }
        Set<Long> tableIds = depTableIds();

        List<TableFieldInfo> fieldInfos = Lambda.queryListIfExist(fieldDicts, v ->
                getFieldInfoService().getBaseMapper().selectList(new QueryWrapper<TableFieldInfo>().lambda()
                        .in(TableFieldInfo::getTableId, tableIds)
                        .in(TableFieldInfo::getRelationColId, v))
        );
        if (CollectionUtils.isEmpty(fieldInfos)) {
            log.warn("field relation is empty table{} dict{}", tableIds, fieldDicts);
        }

        Map<Long, TableFieldInfo> selectFieldMap = fieldInfos.stream()
                .filter(v -> !Objects.equals(applyPeriodFieldId, v.getId().toString()))
                .filter(v -> !metricsQuoteIds.contains(v.getRelationColId()))
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> {
                    // 出现多个表关联同一个字段库字段时，优先使用本表字段
                    if (Objects.equals(front.getTableId(), application.getBaseTableId())) {
                        return front;
                    }
                    if (Objects.equals(current.getTableId(), application.getBaseTableId())) {
                        return current;
                    }
                    return current;
                }));

        return SelectFieldContext.builder()
                .fieldInfos(fieldInfos)
                .selectFieldMap(selectFieldMap)
                .metricsQuoteIds(metricsQuoteIds)
                .build();
    }

    private static class SingleLayerCtx {
        public final StringBuilder result;
        public final String groupBy;
        public final String applyWhere;
        public final String templateWhere;
        private final String tableName;

        public SingleLayerCtx(StringBuilder result, String tableName, String groupBy, String applyWhere, String templateWhere) {
            this.result = result;
            this.tableName = tableName;
            this.groupBy = groupBy;
            this.applyWhere = applyWhere;
            this.templateWhere = templateWhere;
        }
    }
}
