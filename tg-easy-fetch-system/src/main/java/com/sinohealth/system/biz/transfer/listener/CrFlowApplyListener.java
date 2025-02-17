package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.SelectFieldDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.util.HistoryApplyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支持常规和通用 模板Excel导入 表头为
 * <p>
 * 需求ID	需求名称	需求描述	申请人	需求性质	合同编号	尚书台工作流	项目名称	常规交付周期	交付时间延迟天数	模板名	数据有效时间	时间粒度	时间字段	时间范围	市场粒度	市场字段	市场范围	产品粒度	产品字段	产品范围	会员粒度	会员字段	会员范围	其他粒度	其他字段	其他范围	指标列	TOP设置-模式	TOP设置-时间条件	TOP设置-最近N	TOP设置-时间粒度	TOP设置-Top数	TOP设置-目标对象字段	TOP设置-排序字段	TOP设置-分组字段	关联自定义列	对比结果核查
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-07-09 14:05
 */
@Slf4j
public class CrFlowApplyListener extends CrApplyListener {

    public CrFlowApplyListener(HttpServletRequest req, DataSourceTransactionManager dataSourceTransactionManager,
                               TransactionDefinition transactionDefinition, Validator validator) {
        super(req, dataSourceTransactionManager, transactionDefinition, validator);
    }

    /**
     * @param headMap
     * @param context
     */
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        handleTransaction(() -> this.saveCommonApply(this::handleFlowApplyDetail, getKey()));
        list.clear();
    }

    String getKey() {
        return RedisKeys.FlowApply.TRANS_APPLY_MAP;
    }

    /**
     * 工作流 处理 粒度 指标
     */
    void handleFlowApplyDetail(CrApplyVO vo, TgTemplateInfo template, TgApplicationInfo info) {
        // 粒度和字段处理
        List<TemplateGranularityDto> granularityList = template.getGranularity();
        List<ApplicationGranularityDto> applyGras = new ArrayList<>();
        for (TemplateGranularityDto tempGra : granularityList) {
            ApplicationGranularityDto gra;
            if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.time.name())) {
                gra = this.handleFlowTimeGra(vo, template, tempGra);
            } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.product.name())) {
                gra = this.handleFlowProductGra(vo, template, tempGra);
            } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.area.name())) {
                gra = this.handleFlowAreaGra(vo, template, tempGra);
            } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.other.name())) {
                gra = this.handleFlowOtherGra(vo, template, tempGra);
            } else {
                gra = null;
                if (CollectionUtils.isNotEmpty(tempGra.getDetails())) {
                    log.error("未填写粒度 {} 粒度数：{}", tempGra.getGranularity(), tempGra.getDetails().size());
                }
            }
            Optional.ofNullable(gra).ifPresent(applyGras::add);
        }
        info.setGranularity(applyGras);

        // 指标列
        if (CollectionUtils.isNotEmpty(template.getCustomMetrics())) {
            List<String> select = vo.parseMetrics();
            List<Long> metricsIds = Lambda.buildList(template.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId);
            List<MetricsDict> metrics = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>()
                    .lambda().in(MetricsDict::getId, metricsIds));

            Map<String, MetricsDict> metricsDictMap = Lambda.buildMap(metrics, MetricsDict::getFieldName);
            String noDef = select.stream().filter(v -> !metricsDictMap.containsKey(v)).collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(noDef)) {
                throw new CustomException(noDef + " 指标未在模板定义");
            }

            List<CustomMetricsLabelDto> labels = metrics.stream().map(dict -> {
                CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
                dto.setMetricsId(dict.getId());
                dto.setAlias(dict.getName());
                dto.setSelect(select.contains(dict.getFieldName()));
                return dto;
            }).collect(Collectors.toList());
            info.setCustomMetrics(labels);
        }
    }

    private ApplicationGranularityDto handleFlowTimeGra(CrApplyVO vo, TgTemplateInfo template, TemplateGranularityDto tempGra) {
        Optional<TemplateGranularityDetailDto> row = tempGra.getDetails().stream()
                .filter(v -> Objects.equals(v.getName(), vo.getPeriodGranular()))
                .findAny();
        if (!row.isPresent()) {
            throw new CustomException("时间信息 异常，粒度未配置: " + vo.getPeriodGranular());
        }

        // 自选维度才会需要优化
        boolean select = Objects.equals(template.getColAttr(), 1L);
        Set<String> selectPeriod = vo.parsePeriodCols();

        List<SelectFieldDto> selectFieldDtos = Stream.of(row.get().getRequired(), row.get().getOptions())
                .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                .distinct()
                .map(v -> {
                    String fieldName = fieldDictFeildNameMap.get(v);
                    if (select && !selectPeriod.contains(fieldName)) {
                        return null;
                    }

                    // 特殊处理 时间列
                    String alias = Optional.ofNullable(fieldDictNameMap.get(v))
                            .orElse(v == -1L ? ApplicationConst.PeriodField.PERIOD_TYPE_ALIAS : null);
                    if (StringUtils.isBlank(alias)) {
                        throw new CustomException("存在未填写别名的字段");
                    }
                    return SelectFieldDto.builder()
                            .fieldId(v)
                            .alias(alias)
                            .build();
                }).filter(Objects::nonNull).collect(Collectors.toList());

        ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                .granularity(tempGra.getGranularity())
                .selectGranularity(Collections.singletonList(row.get().getName()))
                .fields(selectFieldDtos)
                .build();


        if (StringUtils.isBlank(vo.getPeriodScope()) || Objects.equals(vo.getPeriodScope(), "1=1")) {
            log.info("ignore");
        } else {
            String period = HistoryApplyUtil.convertPeriod(vo.getPeriodScope());
            Optional<FilterDTO> filterOpt = this.parseForFront(period);
            if (filterOpt.isPresent()) {
                applicationService.fillFieldIdForFilter(template, FieldGranularityEnum.time, filterOpt.get());
                tmp.setFilter(filterOpt.get());
            } else {
                throw new CustomException("时间范围解析失败");
            }
        }


        // 季度 截止时间处理
//                    if (Objects.equals(vo.getPeriodGranular(), "季度")) {
//                        FilterDTO.FilterItemDTO item = tmp.getFilter().getFilters().get(0).getFilters().get(1).getFilterItem();
////                        if (Objects.equals(item.getValue(), "2023-12-31")) {
////                        }
//                        try {
//                            String target = "2023-09-01";
//                            Date targetEnd = new SimpleDateFormat("yyyy-MM-dd").parse(target);
//                            Date end = new SimpleDateFormat("yyyy-MM-dd").parse(item.getValue().toString());
//                            if (end.after(targetEnd)) {
//                                item.setValue(target);
//                            }
//                        } catch (Exception e) {
//                            log.error("", e);
//                        }
//                    }
        return tmp;
    }

    private ApplicationGranularityDto handleFlowProductGra(CrApplyVO vo, TgTemplateInfo template, TemplateGranularityDto tempGra) {
        if (CollectionUtils.isEmpty(tempGra.getDetails())) {
            throw new CustomException("产品未配置粒度");
        }

        String productGranular = vo.getProductGranular();
        TemplateGranularityDetailDto first = tempGra.getDetails().stream()
                .filter(v -> Objects.equals(v.getName(), productGranular)).findFirst().orElse(null);
        if (Objects.isNull(first)) {
            throw new CustomException("产品粒度[" + productGranular + "]找不到");
        }

        // 自选维度才会需要优化
        boolean select = Objects.equals(template.getColAttr(), 1L);

        Set<String> cols = vo.parseProductCols();

        List<SelectFieldDto> selectFieldDtos = Stream.of(first.getRequired(), first.getOptions())
                .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                .distinct()
                .map(v -> {
                    String fieldName = fieldDictFeildNameMap.get(v);
                    if (select && !cols.contains(fieldName)) {
                        return null;
                    }

                    return SelectFieldDto.builder()
                            .fieldId(v)
                            .alias(fieldDictNameMap.get(v))
                            .build();
                }).filter(Objects::nonNull).collect(Collectors.toList());

        ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                .granularity(tempGra.getGranularity())
                .selectGranularity(Collections.singletonList(first.getName()))
                .fields(selectFieldDtos)
                .build();

        if (StringUtils.isBlank(vo.getProductScope()) || Objects.equals(vo.getProductScope(), "1=1")) {
            log.info("ignore");
        } else {
            Optional<FilterDTO> filterOpt = this.parseForFront(vo.getProductScope());
            if (filterOpt.isPresent()) {
                applicationService.fillFieldIdForFilter(template, FieldGranularityEnum.product, filterOpt.get());
                tmp.setFilter(filterOpt.get());
            } else {
                throw new CustomException("产品范围解析失败");
            }
        }

        return tmp;
    }

    private ApplicationGranularityDto handleFlowAreaGra(CrApplyVO vo, TgTemplateInfo template, TemplateGranularityDto tempGra) {
        String[] parts = StringUtils.split(vo.getAreaGranular(), ";");
        List<TemplateGranularityDetailDto> selectList = Stream.of(parts)
                .map(v -> tempGra.getDetails().stream().filter(d -> Objects.equals(d.getName(), v)).findAny())
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        List<String> names = Lambda.buildList(selectList, TemplateGranularityDetailDto::getName);
        if (selectList.size() != parts.length) {
            String missing = Stream.of(parts).filter(v -> !names.contains(v)).collect(Collectors.joining(","));
            throw new CustomException("市场信息 异常，粒度未配置: " + missing);
        }

        boolean select = Objects.equals(template.getColAttr(), 1L);
        Set<String> cols = vo.parseAreaCols();
        List<SelectFieldDto> selectFieldDtos =
                selectList.stream()
                        .flatMap(v -> Stream.of(v.getRequired(), v.getOptions()))
                        .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                        .distinct()
                        .map(v -> {
                            String fieldName = fieldDictFeildNameMap.get(v);
                            if (select && !cols.contains(fieldName)) {
                                return null;
                            }
                            return SelectFieldDto.builder()
                                    .fieldId(v)
                                    .alias(fieldDictNameMap.get(v))
                                    .build();
                        }).filter(Objects::nonNull).collect(Collectors.toList());

        ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                .granularity(tempGra.getGranularity())
                .selectGranularity(names)
                .fields(selectFieldDtos)
                .build();

        // 市场范围 特殊处理
        String areaSQL;
        if (StringUtils.isBlank(vo.getAreaScope())
                || vo.getAreaScope().contains("1=1")
                || vo.getAreaScope().contains("1 = 1")) {
            String area = Stream.of(StringUtils.split(vo.getAreaGranular(), "、"))
                    .map(v -> "'" + v + "'").collect(Collectors.joining(","));
            areaSQL = String.format(" region_type in (%s)", area);
        } else {
            areaSQL = vo.getAreaScope().replace("a.", "");
            areaSQL = areaSQL.replace("city_co_name", "city");
        }

        Optional<FilterDTO> filterOpt = this.parseForFront(areaSQL);
        if (filterOpt.isPresent()) {
            applicationService.fillFieldIdForFilter(template, FieldGranularityEnum.area, filterOpt.get());
            tmp.setFilter(filterOpt.get());
        } else {
            throw new CustomException("市场范围解析失败");
        }

        return tmp;
    }

    private ApplicationGranularityDto handleFlowOtherGra(CrApplyVO vo, TgTemplateInfo template, TemplateGranularityDto tempGra) {
        String[] parts = StringUtils.split(vo.getOtherGranular(), ";");
        if (Objects.isNull(parts)) {
            return null;
        }
        // 判断粒度
        List<TemplateGranularityDetailDto> selectList = Stream.of(parts)
                .map(v -> tempGra.getDetails().stream().filter(d -> Objects.equals(d.getName(), v)).findAny())
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        List<String> names = Lambda.buildList(selectList, TemplateGranularityDetailDto::getName);
        if (selectList.size() != parts.length) {
            String missing = Stream.of(parts).filter(v -> !names.contains(v)).collect(Collectors.joining(","));
            throw new CustomException(template.getName() + " 其他信息 异常，粒度未配置: " + missing);
        }

        boolean select = Objects.equals(template.getColAttr(), 1L);
        Set<String> cols = vo.parseOtherCols();

        List<SelectFieldDto> selectFieldDtos =
                selectList.stream()
                        .flatMap(v -> Stream.of(v.getRequired(), v.getOptions()))
                        .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                        .distinct()
                        .map(v -> {
                            String fieldName = fieldDictFeildNameMap.get(v);
                            if (select && !cols.contains(fieldName)) {
                                return null;
                            }

                            return SelectFieldDto.builder()
                                    .fieldId(v)
                                    .alias(fieldDictNameMap.get(v))
                                    .build();
                        }).filter(Objects::nonNull).collect(Collectors.toList());

        ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                .granularity(tempGra.getGranularity())
                .selectGranularity(names)
                .fields(selectFieldDtos)
                .build();

        return tmp;
    }


}
