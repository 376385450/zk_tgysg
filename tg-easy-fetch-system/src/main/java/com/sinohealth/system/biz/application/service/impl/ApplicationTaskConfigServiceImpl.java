package com.sinohealth.system.biz.application.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.bi.data.AbstractDatabase;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.MySql;
import com.sinohealth.bi.data.Table;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsQcScopeEnum;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.constants.TopPeriodTypeEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigSnapshotDAO;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfigSnapshot;
import com.sinohealth.system.biz.application.dto.ApplicationDistributedInfo;
import com.sinohealth.system.biz.application.dto.ApplicationDistributedSectionInfo;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.SelectFieldDto;
import com.sinohealth.system.biz.application.dto.TopSettingApplyDto;
import com.sinohealth.system.biz.application.service.ApplicationTaskConfigService;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.service.FieldDictService;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.service.DataRangeTemplateService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.QuerySqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-07 09:54
 */
@Slf4j
@Service
public class ApplicationTaskConfigServiceImpl implements ApplicationTaskConfigService {

    /**
     * 单字段 多业务值时的分隔
     */
    private static final String DELIMITER = ";";
    /**
     * 分隔 数组 或 同业务但多值
     */
    private static final String SAME_DELIMITER = ",";

    @Autowired
    private ApplicationTaskConfigDAO applicationTaskConfigDAO;
    @Autowired
    private ApplicationTaskConfigSnapshotDAO taskConfigSnapshotDAO;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private FieldDictDAO fieldDictDAO;
    @Autowired
    private MetricsDictDAO metricsDictDAO;

    @Autowired
    private FieldDictService fieldDictService;
    @Autowired
    private DataRangeTemplateService dataRangeTemplateService;
    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ITemplateService templateService;

    @Autowired
    private AppProperties appProperties;


    @Scheduled(cron = "0 0 0 * * ? ")
    public void checkInvalidConfig() {
        List<ApplicationTaskConfig> invalid = applicationTaskConfigDAO.lambdaQuery()
                .lt(ApplicationTaskConfig::getDataExpir, new Date())
                .eq(ApplicationTaskConfig::getActive, true).list();
        if (CollectionUtils.isEmpty(invalid)) {
            return;
        }

        Set<Long> ids = Lambda.buildSet(invalid, ApplicationTaskConfig::getId);
        applicationTaskConfigDAO.lambdaUpdate().in(ApplicationTaskConfig::getId, ids)
                .set(ApplicationTaskConfig::getActive, false).update();
    }

    /**
     * 保存工作流模板申请 配置参数
     */
    @Override
    public void saveApplicationTaskConfig(TgApplicationInfo applyInfo) {
        try {
            log.info("Config: {}", JsonUtils.format(applyInfo));
            if (Objects.isNull(applyInfo.getTemplateId())) {
                return;
            }

            TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(applyInfo.getTemplateId());
            Project project = projectDAO.getBaseMapper().selectById(applyInfo.getProjectId());

            ApplicationTaskConfig config = new ApplicationTaskConfig();
            Long userId = SecurityUtils.getUserId();
            config.setCreator(userId);
            config.setUpdater(userId);
            config.setApplicationNo(applyInfo.getApplicationNo());
            boolean isPersistence = Objects.equals(applyInfo.getRequireTimeType(), ApplicationConst.RequireTimeType.PERSISTENCE);
            config.setProjectIsConst(isPersistence ? ApplicationConst.RequireTimeTypeEnum.PERSISTENCE.getDesc() : ApplicationConst.RequireTimeTypeEnum.ONCE.getDesc());

            // 填充粒度 字段信息 自定义列信息
            this.fillGranularityAndRangeTemplate(applyInfo.getGranularity(), config);
            // 开启了分布信息，要组装sql
            if (Objects.nonNull(applyInfo.getOpenDistributed())
                    && applyInfo.getOpenDistributed()
                    && CollectionUtils.isNotEmpty(applyInfo.getDistributeds())) {
                config.setZdyQj(this.buildZdyQj(applyInfo.getDistributeds()));
            }

            boolean qc = BooleanUtils.isTrue(templateInfo.getAssetsQc()) && BooleanUtils.isTrue(applyInfo.getAssetsQc());
            config.setApplicationId(applyInfo.getId())
                    .setApplicationName(applyInfo.getProjectName())
                    .setProjectNo("pro_" + project.getId())
                    .setProjectName(project.getName())
                    .setProjectUser(ApplyUtil.userName(applyInfo.getApplicantName()))
                    .setProjectUnit(applyInfo.getApplicantDepartment())
                    .setProjectManager(userService.getUserViewName(project.getProjectManager()))
                    .setProjectBackground(project.getDescription())
                    .setProjectType(RequireAttrType.DESC_MAP.get(applyInfo.getRequireAttr()))
                    .setProjectHtno(applyInfo.getContractNo())
                    .setBusinessLine(BizTypeEnum.getDesc(templateInfo.getBizType()))
                    .setValidDate(fmtDate(applyInfo.getDataExpir()))
                    .setExpectDate(fmtDate(applyInfo.getExpectTime()))
                    .setProjectUpdateFre(DeliverTimeTypeEnum.getTypeDesc(applyInfo.getDeliverTimeType()))
                    .setRegularLeadPeriod(String.valueOf(applyInfo.getDeliverDelay()))
                    .setBusinessBlock(templateInfo.getTemplateName())
                    .setZdyParam(applyInfo.getCustomExt())
                    .setRemark(applyInfo.getApplyRemark())
                    .setAssetsQc(qc ? "是" : "否")
                    .setPushPowerBi(applyInfo.getPushPowerBi())
                    .setProjectScope(AssetsQcScopeEnum.getDesc(applyInfo.getProjectScope()))
                    .setDataExpir(applyInfo.getDataExpir())
                    .setActive(true)
                    // 自定义标签
                    .setCustomTag(applyInfo.getCustomTag())
                    .setTagProjectName(applyInfo.getTagProjectName())
                    .setTagTags(applyInfo.getTagTags())
                    .setTagClient(applyInfo.getTagClient())
                    .setTagTableName(applyInfo.getTagTableName())
                    .setTagIds(applyInfo.getTagIds())
                    .setTagNewField(applyInfo.getTagNewField())
                    .setTagCascade(applyInfo.getTagCascade());

            this.fillFlowName(applyInfo, templateInfo, config);

            List<CustomMetricsLabelDto> metrics = applyInfo.getCustomMetrics();
            if (CollectionUtils.isNotEmpty(metrics)) {
                Set<Long> metricIds = metrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toSet());
                List<MetricsDict> dicts = metricsDictDAO.listByIds(metricIds);
                Map<Long, MetricsDict> dictMap = Lambda.buildMap(dicts, MetricsDict::getId);

                List<String> name = new ArrayList<>();
                List<String> alias = new ArrayList<>();
                metrics.stream().filter(v -> BooleanUtils.isTrue(v.getSelect())).forEach(v -> {
                    MetricsDict dict = dictMap.get(v.getMetricsId());
                    if (Objects.isNull(dict)) {
                        throw new CustomException("依赖指标被删除，请检查配置: " + v.getAlias() + "(" + v.getMetricsId() + ")");
                    }
                    alias.add(StrUtil.firstNotBlankStr(v.getAlias(), dict.getName()));
                    name.add(dict.getFieldName());
                });
                config.setDataKpi(combineCols(name, alias));
            }

            this.fillZdyParam(config);
            this.fillTopSetting(applyInfo, config);

            log.info("create config={}", config);
            applicationTaskConfigDAO.remove(new QueryWrapper<ApplicationTaskConfig>().lambda().eq(ApplicationTaskConfig::getApplicationId, config.getApplicationId()));
            applicationTaskConfigDAO.saveOrUpdate(config);

            ApplicationTaskConfigSnapshot snapshot = new ApplicationTaskConfigSnapshot();
            BeanUtils.copyProperties(config, snapshot);
            snapshot.setConfigId(config.getId());
            snapshot.setId(null);
            snapshot.setCreateTime(LocalDateTime.now());
            taskConfigSnapshotDAO.save(snapshot);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void fillFlowName(TgApplicationInfo applyInfo, TgTemplateInfo templateInfo, ApplicationTaskConfig config) {
        Boolean taskType = TemplateTypeEnum.of(templateInfo.getTemplateType()).map(TemplateTypeEnum::isSchedulerTaskType).orElse(false);
        if (!taskType) {
            return;
        }

        Integer finalSchedulerId = DataAssetsUtil.getFinalSchedulerId(templateInfo, applyInfo);
        if (Objects.isNull(finalSchedulerId)) {
            log.warn("no flow id: {}", applyInfo.getId());
            return;
        }

        try {
            AjaxResult processResult = intergrateProcessDefService.queryProcessById(finalSchedulerId);
            if (!processResult.isDolphinSuccess()) {
                log.warn("processResult={}", processResult);
                return;
            }

            Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
            config.setFlowName(Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     *
     */
    @Override
    public AjaxResult<Void> appendSaveApplyConfig(Integer batch) {
        List<TgApplicationInfo> allApply = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId)
                .ge(TgApplicationInfo::getDataExpir, new Date())
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .list();

        Set<Long> applyIds = Lambda.buildSet(allApply, TgApplicationInfo::getId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return AjaxResult.success("Empty", null);
        }

        List<ApplicationTaskConfig> list = applicationTaskConfigDAO.lambdaQuery()
                .select(ApplicationTaskConfig::getApplicationId)
                .in(ApplicationTaskConfig::getApplicationId, applyIds).list();
        Set<Long> exists = Lambda.buildSet(list, ApplicationTaskConfig::getApplicationId);

        List<TgApplicationInfo> needApply = applicationDAO.lambdaQuery()
                .notIn(CollectionUtils.isNotEmpty(exists), TgApplicationInfo::getId, exists)
                .ge(TgApplicationInfo::getDataExpir, new Date())
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .last(Objects.nonNull(batch), " limit " + batch).list();
        log.info("all={} exist={} need={}", applyIds.size(), exists.size(), needApply.size());
        for (TgApplicationInfo info : needApply) {
            try {
                JsonBeanConverter.convert2Obj(info);
                this.saveApplicationTaskConfig(info);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return AjaxResult.succeed();
    }

    private String combineCols(List<String> names, List<String> alias) {
        if (CollectionUtils.isEmpty(names) || CollectionUtils.isEmpty(alias)) {
            return "";
        }
        return String.format("[%s]" + DELIMITER + "[%s]", String.join(SAME_DELIMITER, names), String.join(SAME_DELIMITER, alias));
    }

    // @formatter:off

    /**
     * 处理自定义参数
     *
     * <pre>
     * 1. 字段类型处理：
     * {
     * "name": "关联分析1",
     * "note": "关联分析设置2",
     * "tips": "测试自适应设置项2",
     * "options": [87, 88, 89],
     * "subType": "field",
     * "required": 0,
     * "customGranularity": true,
     * "bool": true,
     * "select": [87, 88]
     * }
     * 2. 日期类型处理
     * {
     * "note": "日期",
     * "select": ["`create_time` >= '2023-06-01'"],
     * "bool": true,
     * "name": "AB",
     * "options": [75],
     * "subType": "date",
     * "customGranularity": false,
     * "tips": "",
     * "required": 0,
     * "dateFilter": {
     * "timeViewName": "2023-05",
     * "timeDimension": "month",
     * "value": "2023-05-01",
     * "functionalOperator": "moreThan"
     * }
     * }
     * </pre>
     */
    // @formatter:on
    private void fillZdyParam(ApplicationTaskConfig config) {
        String zdyParam = config.getZdyParam();
        if (StringUtils.isBlank(zdyParam)) {
            return;
        }

        Set<Long> ids = new HashSet<>();
        JSONArray params = JSON.parseArray(zdyParam);
        // 分组遍历处理数据
        Map<String, Boolean> groupSwitchMap = new HashMap<>();
        JSONArray newParams = new JSONArray();
        for (Object param : params) {
            JSONObject obj = (JSONObject) param;
            // 校验该段配置是否需要传递
            String groupSetting = obj.getString("groupSetting");
            if (StringUtils.isNotBlank(groupSetting)) {
                if (!groupSwitchMap.containsKey(groupSetting)) {
                    Boolean groupSwitch = obj.getBoolean("groupSwitch");
                    boolean flag = Objects.isNull(groupSwitch) || groupSwitch;
                    groupSwitchMap.put(groupSetting, flag);
                }
                if (groupSwitchMap.containsKey(groupSetting) && groupSwitchMap.get(groupSetting)) {
                    // 去除多余字段，不影响尚书台解析流程
                    obj.remove("groupSetting");
                    obj.remove("groupSwitch");
                    newParams.add(obj);
                }
            }
        }
        params = newParams;
        config.setZdyParam(params.toJSONString());

        for (Object param : params) {
            JSONObject obj = (JSONObject) param;
            String subType = obj.getString("subType");
            // 日期组件
            if (Objects.equals(subType, "date")) {
                JSONArray select = obj.getJSONArray("select");
                select.stream().map(Object::toString).map(Long::valueOf).forEach(ids::add);
                continue;
            }

            // 字段组件
            if (!Objects.equals(subType, "field")) {
                continue;
            }

            JSONArray opts = obj.getJSONArray("options");
            opts.stream().map(Object::toString).map(Long::valueOf).forEach(ids::add);
            JSONArray select = obj.getJSONArray("select");
            select.stream().map(Object::toString).map(Long::valueOf).forEach(ids::add);
        }
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        List<FieldDict> fields = fieldDictDAO.listByIds(ids);
        Map<Long, String> nameMap = Lambda.buildMap(fields, FieldDict::getId, FieldDict::getFieldName);
        for (Object param : params) {
            JSONObject obj = (JSONObject) param;
            String subType = obj.getString("subType");
            if (Objects.equals(subType, "date")) {
                FilterDTO.FilterItemDTO dateFilter = JsonUtils.parse(obj.getString("dateFilter"), new TypeReference<FilterDTO.FilterItemDTO>() {
                });
                ;
                JSONArray select = obj.getJSONArray("select");
                Optional<Long> idOpt = select.stream().map(Object::toString).map(Long::valueOf).findFirst();
                if (!idOpt.isPresent()) {
                    throw new RuntimeException("请选择其他设置中的时间类型设置项的目标字段");
                }
                dateFilter.setFieldName(nameMap.get(idOpt.get()));
                String sql = QuerySqlUtil.buildMySQLWhere("TABLE", Collections.singletonList(dateFilter));
                sql = sql.replace("TABLE.", "");
                obj.put("select", JSON.parseArray("[\"" + sql + "\"]"));
                continue;
            }
            if (!Objects.equals(subType, "field")) {
                continue;
            }

            JSONArray opts = obj.getJSONArray("options");
            JSONArray select = obj.getJSONArray("select");

            String optNames = opts.stream().map(Object::toString).map(Long::valueOf).map(nameMap::get).map(v -> "\"" + v + "\"").collect(Collectors.joining(SAME_DELIMITER));
            obj.put("options", JSON.parseArray("[" + optNames + "]"));


            String selectNames = select.stream().map(Object::toString).map(Long::valueOf).map(nameMap::get).map(v -> "\"" + v + "\"").collect(Collectors.joining(SAME_DELIMITER));
            obj.put("select", JSON.parseArray("[" + selectNames + "]"));
        }
        config.setZdyParam(params.toJSONString());
    }

    /**
     * Top设置
     */
    private void fillTopSetting(TgApplicationInfo applyInfo, ApplicationTaskConfig config) {
        TopSettingApplyDto top = applyInfo.getTopSetting();
        if (Objects.isNull(top) || BooleanUtils.isNotTrue(top.getEnable())) {
            return;
        }

        Set<Long> ids = new HashSet<>();
        Optional.ofNullable(top.getGroupField()).ifPresent(ids::addAll);
        Optional.ofNullable(top.getTargetField()).ifPresent(ids::add);

        Map<Long, String> fieldMap = Lambda.queryMapIfExist(ids, fieldDictDAO::listByIds, FieldDict::getId, FieldDict::getName);
        List<String> parts = new ArrayList<>();

        if (Objects.equals(top.getPeriodType(), TopPeriodTypeEnum.fixed.getCode())) {
            // 日期SQL
            top.getDateFilter().setFieldName("period");
            String sql = QuerySqlUtil.buildMySQLWhere("TABLE", Collections.singletonList(top.getDateFilter()));
            sql = sql.replace("TABLE.", "");
            config.setTopPeriodType(TopPeriodTypeEnum.fixed.getDesc());
            config.setTopPeriod(sql);
        } else {
            config.setTopPeriodType(TopPeriodTypeEnum.dynamic.getDesc());
            config.setTopPeriod(top.getLastDuration() + DELIMITER + top.getDurationType());
        }

        // 分组可以多选
        if (CollectionUtils.isNotEmpty(top.getGroupField())) {
            parts.add(top.getGroupField().stream().map(fieldMap::get).collect(Collectors.joining("/")));
        } else {
            parts.add("");
        }

        parts.add(Optional.ofNullable(top.getSortField()).map(metricsDictDAO::getById).map(MetricsDict::getName).orElse(""));

        if (Objects.nonNull(top.getTopNum())) {
            parts.add(String.valueOf(top.getTopNum()));
        } else {
            parts.add("");
        }

        if (Objects.nonNull(top.getTargetField())) {
            parts.add(fieldMap.get(top.getTargetField()));
        } else {
            parts.add("");
        }

        if (Objects.nonNull(top.getOthersPack()) && top.getOthersPack()) {
            parts.add("是");
        } else {
            parts.add("否");
        }

        config.setTopKpi(String.join(DELIMITER, parts));
    }

    private String fmtDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
        return fmt.format(date);
    }

    private void fillGranularityAndRangeTemplate(List<ApplicationGranularityDto> granularity, ApplicationTaskConfig config) {
        if (CollectionUtils.isEmpty(granularity)) {
            log.warn("no granularity");
            return;
        }
        boolean allEmpty = granularity.stream().allMatch(v -> CollectionUtils.isEmpty(v.getSelectGranularity()) && CollectionUtils.isEmpty(v.getFields()));
        if (allEmpty) {
            log.warn("no granularity");
            return;
        }
        Map<String, ApplicationGranularityDto> granularityDtoMap = granularity.stream().collect(Collectors.toMap(ApplicationGranularityDto::getGranularity, v -> v, (front, current) -> current));
        ApplicationGranularityDto timeDTO = granularityDtoMap.get(FieldGranularityEnum.time.name());
        ApplicationGranularityDto areaDTO = granularityDtoMap.get(FieldGranularityEnum.area.name());
        ApplicationGranularityDto productDTO = granularityDtoMap.get(FieldGranularityEnum.product.name());
        ApplicationGranularityDto memberDTO = granularityDtoMap.get(FieldGranularityEnum.member.name());
        ApplicationGranularityDto otherDTO = granularityDtoMap.get(FieldGranularityEnum.other.name());

        this.fillZdySQL(config, timeDTO, areaDTO, productDTO, memberDTO, otherDTO);

        fieldDictService.fillFieldNameForFilter(timeDTO, areaDTO, productDTO, memberDTO, otherDTO);

        Set<Long> fieldIds = this.extractId(timeDTO, areaDTO, productDTO, memberDTO, otherDTO);
        List<FieldDict> fieldDicts = Lambda.queryListIfExist(fieldIds, fieldDictDAO::listByIds);
        Map<Long, String> fieldNameMap = Lambda.buildMap(fieldDicts, FieldDict::getId, FieldDict::getFieldName);

        fieldNameMap.putAll(ApplicationConst.PeriodField.idToNameMap);

        this.fillGranularity(timeDTO, fieldNameMap, config::setPeriodDataCol, config::setPeriodGranular, config::setPeriodScope);
        this.fillGranularity(areaDTO, fieldNameMap, config::setAreaDataCol, config::setAreaGranular, config::setAreaScope);
        this.fillGranularity(productDTO, fieldNameMap, config::setProductDataCol, config::setProductGranular, config::setProductScope);
        this.fillGranularity(memberDTO, fieldNameMap, config::setMemberDataCol, config::setMemberGranular, config::setMemberScope);
        this.fillGranularity(otherDTO, fieldNameMap, config::setOtherDataCol, config::setOtherGranular, config::setOtherScope);
    }

    private void fillZdySQL(ApplicationTaskConfig config, ApplicationGranularityDto timeDTO, ApplicationGranularityDto areaDTO, ApplicationGranularityDto productDTO, ApplicationGranularityDto memberDTO, ApplicationGranularityDto otherDTO) {
        Set<Long> rangeIdSet = Stream.of(areaDTO, timeDTO, productDTO, memberDTO, otherDTO).filter(Objects::nonNull).map(ApplicationGranularityDto::getRangeTemplateId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(rangeIdSet)) {
            return;
        }
        // case when 条件组 SQL
        log.info("查询自定义sql：{}", rangeIdSet);
        Map<Long, String> sqlMap = dataRangeTemplateService.buildTargetSqlMap(rangeIdSet, config.getApplicationNo());
        //自定义时间
        if (timeDTO != null && timeDTO.getRangeTemplateId() != null) {
            config.setZdyPeriod(sqlMap.getOrDefault(timeDTO.getRangeTemplateId(), ""));
        }
        //自定义市场
        if (areaDTO != null && areaDTO.getRangeTemplateId() != null) {
            config.setZdyArea(sqlMap.getOrDefault(areaDTO.getRangeTemplateId(), ""));
        }
        if (productDTO != null && productDTO.getRangeTemplateId() != null) {
            String sql = sqlMap.get(productDTO.getRangeTemplateId());
            config.setZdyProduct(sql == null ? "" : dataRangeTemplateService.getCreateTablePre(config.getApplicationNo(), sql));
        }
        if (memberDTO != null && memberDTO.getRangeTemplateId() != null) {
            config.setZdyMember(sqlMap.getOrDefault(memberDTO.getRangeTemplateId(), ""));
        }
        if (otherDTO != null && otherDTO.getRangeTemplateId() != null) {
            config.setZdyOther(sqlMap.getOrDefault(otherDTO.getRangeTemplateId(), ""));
        }
    }

    private Set<Long> extractId(ApplicationGranularityDto... fields) {
        return Stream.of(fields).filter(Objects::nonNull)
                .map(ApplicationGranularityDto::getFields)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(SelectFieldDto::getFieldId)
                .collect(Collectors.toSet());
    }

    public void fillGranularity(ApplicationGranularityDto dto,
                                Map<Long, String> fieldNameMap,
                                Function<String, ApplicationTaskConfig> dataCol,
                                Function<String, ApplicationTaskConfig> granular,
                                Function<String, ApplicationTaskConfig> scope) {
        if (Objects.isNull(dto)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(dto.getSelectGranularity())) {
            granular.apply(String.join(DELIMITER, dto.getSelectGranularity()));
        }

        if (CollectionUtils.isNotEmpty(dto.getFields())) {
            // 前端渲染会需要重复的数据
            Map<Long, String> aliasMap = dto.getFields().stream().collect(Collectors.toMap(SelectFieldDto::getFieldId, SelectFieldDto::getAlias, (front, current) -> current));
            List<String> names = aliasMap.keySet().stream().map(fieldNameMap::get).collect(Collectors.toList());
            List<String> alias = aliasMap.values().stream().distinct().collect(Collectors.toList());

            dataCol.apply(this.combineCols(names, alias));
        }

        if (Objects.nonNull(dto.getFilter()) && Objects.nonNull(scope)) {
            Table table = new Table();
            table.setUniqueId(1L);
            table.setFactTable(true);
            Filter targetFilter = new Filter();
            ApplicationSqlUtil.convertToFilter(dto.getFilter(), targetFilter);

            final MySql mysql = new MySql(Collections.singletonList(table), targetFilter);
            mysql.setHiddenQuote(true);

            String whereSql = mysql.getWhereSql();
            if (StringUtils.isNotBlank(whereSql) && !Objects.equals(whereSql, AbstractDatabase.DEFAULT_CONDITION)) {
                whereSql = whereSql.replace("t_1.", "");
                // 导入数据 特殊处理
                whereSql = whereSql.replace("t_null.", "");
                if (!(whereSql.startsWith("(") && whereSql.endsWith(")"))) {
                    whereSql = "(" + whereSql + ")";
                }
                scope.apply(whereSql);
            }
        }
    }

    /**
     * 构建自定义区间sql
     *
     * @param distributeds 区间定义信息
     * @return 自定义区间sql
     */
    private String buildZdyQj(List<ApplicationDistributedInfo> distributeds) {
        // #传参格式：
        //
        // '分布类型值' as qj_type
        //
        //,case when 分布类型字段>=最小值 and  分布类型字段<最大值
        //
        //    then  '['||(分布类型字段::int/步长)*步长||','||((分布类型字段::int/步长)+1)*步长||')'   --有细分分层
        //
        //    when 分布类型字段>=最小值 and  分布类型字段<最大值 then  '分段名称'
        //
        //     when 分布类型字段>=最小值 then  '分段名称'
        //
        //    when 分布类型字段<最小值 then  '分段名称'
        //
        //   --无细分分层，如果是正无穷 就直接 分布类型字段>=最小值，如果是负无穷，就直接 分布类型值<最大值
        //
        //   else null end as  qj_range
        //
        //同个需求统一传参至 zdy_qj 中，不同组之间用 ;隔开
        List<FieldDictDTO> fieldDicts = templateService.distributedFieldList();
        if (CollectionUtils.isEmpty(fieldDicts)) {
            return null;
        }
        FieldDictDTO typeField = fieldDicts.stream().filter(i -> appProperties.getDistributedTypeFieldId().equals(i.getId())).findFirst().get();
        FieldDictDTO qjField =
                fieldDicts.stream().filter(i -> appProperties.getDistributedQjFieldId().equals(i.getId())).findFirst().get();
        String typeFieldName = typeField.getFieldName();
        String qjFieldName = qjField.getFieldName();

        List<String> sqlList = new ArrayList<>();
        for (ApplicationDistributedInfo i : distributeds) {
            if (CollectionUtils.isEmpty(i.getSectionInfos())) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
//            sb.append("'").append(i.getName()).append("' as qj_type , ");
            sb.append("'").append(i.getName()).append("' " +
                    "as ").append(typeFieldName).append(" ,  ");
            String fieldColumnName = i.getFieldColumnName();

            sb.append(" case ");
            List<ApplicationDistributedSectionInfo> sectionInfos = i.getSectionInfos();
            for (ApplicationDistributedSectionInfo sectionInfo : sectionInfos) {
                if (Objects.nonNull(sectionInfo.getIsRefine()) && sectionInfo.getIsRefine()) {
                    // 根据步长划分
                    BigDecimal left =
                            new BigDecimal(sectionInfo.getLeft()).stripTrailingZeros();
                    BigDecimal right =
                            new BigDecimal(sectionInfo.getRight()).stripTrailingZeros();
                    BigDecimal stepSize =
                            BigDecimal.valueOf(sectionInfo.getStepSize()).stripTrailingZeros();
                    while (left.add(stepSize).compareTo(right) <= 0) {
                        BigDecimal nRight = left.add(stepSize);
                        sb.append(" when ");
                        sb.append(" ").append(fieldColumnName).append(" >= ").append(left.stripTrailingZeros().toPlainString());
                        sb.append(" AND ").append(fieldColumnName).append(" < ").append(nRight.stripTrailingZeros().toPlainString());
                        sb.append(" then '[ ").append(left.stripTrailingZeros().toPlainString()).append(" ~ ").append(nRight.stripTrailingZeros().toPlainString()).append(" )' ");
                        left = nRight;
                    }
                } else {
                    sb.append(" when ");
                    // 负无穷
                    Boolean negativeInfinity = !checkIsNumber(sectionInfo.getLeft());
                    // 正无穷
                    Boolean positiveInfinity = !checkIsNumber(sectionInfo.getRight());
                    if (negativeInfinity && positiveInfinity) {
                        sb.append(" 1 = 1 ");
                    } else if (negativeInfinity) {
                        // 负无穷
                        sb.append(" ").append(fieldColumnName).append(" < ").append(new BigDecimal(sectionInfo.getRight()).stripTrailingZeros().toPlainString());
                    } else if (positiveInfinity) {
                        // 正无穷
                        sb.append(" ").append(fieldColumnName).append(" >= ").append(new BigDecimal(sectionInfo.getLeft()).stripTrailingZeros().toPlainString());
                    } else {
                        sb.append(" ").append(fieldColumnName).append(" >= ").append(new BigDecimal(sectionInfo.getLeft()).stripTrailingZeros().toPlainString());
                        sb.append(" AND ").append(fieldColumnName).append(" < ").append(new BigDecimal(sectionInfo.getRight()).stripTrailingZeros().toPlainString());
                    }
                    sb.append(" then '").append(sectionInfo.getName()).append("' ");
                }
            }
            sb.append(" else null end as  ").append(qjFieldName).append(" ");
            sqlList.add(sb.toString());
        }
        return String.join(" ;", sqlList);
    }

    /**
     * 校验是否为正常数值
     *
     * @param numberString 文本
     * @return 是否为正常数值
     */
    private Boolean checkIsNumber(String numberString) {
        try {
//            Integer.parseInt(numberString);
//            return true;
            Double.parseDouble(numberString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
