//package com.sinohealth.system.biz.transfer.listener;
//
//import com.alibaba.excel.context.AnalysisContext;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.sinohealth.common.core.redis.RedisKeys;
//import com.sinohealth.common.enums.dict.FieldGranularityEnum;
//import com.sinohealth.common.exception.CustomException;
//import com.sinohealth.common.utils.StringUtils;
//import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
//import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
//import com.sinohealth.system.biz.application.dto.SelectFieldDto;
//import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
//import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
//import com.sinohealth.system.biz.application.util.Lambda;
//import com.sinohealth.system.biz.dict.domain.MetricsDict;
//import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
//import com.sinohealth.system.domain.TgApplicationInfo;
//import com.sinohealth.system.domain.TgTemplateInfo;
//import com.sinohealth.system.domain.constant.ApplicationConst;
//import com.sinohealth.system.dto.analysis.FilterDTO;
//import com.sinohealth.system.util.HistoryApplyUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.TransactionDefinition;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Validator;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * @author kuangchengping@sinohealth.cn
// * 2024-07-09 14:04
// */
//@Slf4j
//public class CrCustomWideApplyListener extends CrApplyListener {
//
//    public CrCustomWideApplyListener(HttpServletRequest req,
//                                     DataSourceTransactionManager dataSourceTransactionManager,
//                                     TransactionDefinition transactionDefinition, Validator validator) {
//        super(req, dataSourceTransactionManager, transactionDefinition, validator);
//    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext context) {
//        handleTransaction(() -> this.saveCommonApply(this::handleCustomApplyDetail, RedisKeys.CustomApply.TRANS_APPLY_MAP));
//    }
//
//    /**
//     * 自定义 处理 粒度 指标
//     */
//    private void handleCustomApplyDetail(CrApplyVO vo, TgTemplateInfo template, TgApplicationInfo info) {
//        // 粒度和字段处理
//        List<TemplateGranularityDto> granularityList = template.getGranularity();
//        List<ApplicationGranularityDto> applyGras = new ArrayList<>();
//        for (TemplateGranularityDto tempGra : granularityList) {
//            if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.time.name())) {
//                Optional<TemplateGranularityDetailDto> row = tempGra.getDetails().stream()
//                        .filter(v -> Objects.equals(v.getName(), vo.getPeriodGranular()))
//                        .findAny();
//                if (!row.isPresent()) {
//                    throw new CustomException("时间信息 异常，粒度未配置: " + vo.getPeriodGranular());
//                }
//
//                Set<String> selectPeriod = vo.parsePeriodCols();
//                // 全选是因为固定维度。
//                List<SelectFieldDto> selectFieldDtos = Stream.of(row.get().getRequired(), row.get().getOptions())
//                        .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
//                        .distinct()
//                        .map(v -> {
//                            String fieldName = fieldDictFeildNameMap.get(v);
//                            if (!selectPeriod.contains(fieldName)
//                                    && v != -1 && !Objects.equals(fieldName, "period_new")
//                                    && !Objects.equals(fieldName, "period_type")
//                                    && !Objects.equals(fieldName, "period_str")) {
//                                return null;
//                            }
//
//                            // 特殊处理 时间列
//                            String alias = Optional.ofNullable(fieldDictNameMap.get(v))
//                                    .orElse(v == -1L ? ApplicationConst.PeriodField.PERIOD_TYPE_ALIAS : null);
//                            if (StringUtils.isBlank(alias)) {
//                                throw new CustomException("存在未填写别名的字段");
//                            }
//                            return SelectFieldDto.builder()
//                                    .fieldId(v)
//                                    .alias(alias)
//                                    .build();
//                        }).filter(Objects::nonNull).collect(Collectors.toList());
//
//                ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
//                        .granularity(tempGra.getGranularity())
//                        .selectGranularity(Collections.singletonList(row.get().getName()))
//                        .fields(selectFieldDtos)
//                        .build();
//
//                if (StringUtils.isBlank(vo.getPeriodScope()) || Objects.equals(vo.getPeriodScope(), "1=1")) {
//                    log.info("ignore period scope");
//                } else {
//                    String period = HistoryApplyUtil.convertCommonPeriod(vo.getPeriodScope(), false);
//                    Optional<FilterDTO> filterOpt = this.parseForFront(period);
//
//                    if (filterOpt.isPresent()) {
//                        applicationService.fillFieldIdForFilter(transferProperties.getBaseTableId(), filterOpt.get());
//                        tmp.setFilter(filterOpt.get());
//                    } else {
//                        throw new CustomException("时间范围解析失败");
//                    }
//                }
//
//                applyGras.add(tmp);
//            } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.product.name())) {
//                if (CollectionUtils.isEmpty(tempGra.getDetails())) {
//                    throw new CustomException("产品未配置粒度");
//                }
//
//                String productGranular = vo.getProductGranular();
//                TemplateGranularityDetailDto first = tempGra.getDetails().stream()
//                        .filter(v -> Objects.equals(v.getName(), productGranular)).findFirst().orElse(null);
//                if (Objects.isNull(first)) {
//                    throw new CustomException("产品粒度[" + productGranular + "]找不到");
//                }
//
//                Set<String> cols = vo.parseProductCols();
//
//                List<SelectFieldDto> selectFieldDtos = Stream.of(first.getRequired(), first.getOptions())
//                        .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
//                        .distinct()
//                        .map(v -> {
//                            String fieldName = fieldDictFeildNameMap.get(v);
//                            if (!cols.contains(fieldName)) {
//                                return null;
//                            }
//                            return SelectFieldDto.builder()
//                                    .fieldId(v)
//                                    .alias(fieldDictNameMap.get(v))
//                                    .build();
//                        })
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
//
//                ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
//                        .granularity(tempGra.getGranularity())
//                        .selectGranularity(Collections.singletonList(first.getName()))
//                        .fields(selectFieldDtos)
//                        .build();
//
//                if (StringUtils.isBlank(vo.getProductScope()) || Objects.equals(vo.getProductScope(), "1=1")) {
//                    log.info("ignore product scope");
//                } else {
//                    Optional<FilterDTO> filterOpt = this.parseForFront(vo.getProductScope());
//                    if (filterOpt.isPresent()) {
//                        applicationService.fillFieldIdForFilter(transferProperties.getBaseTableId(), filterOpt.get());
//                        tmp.setFilter(filterOpt.get());
//                    } else {
//                        throw new CustomException("产品范围解析失败");
//                    }
//                }
//
//                applyGras.add(tmp);
//            } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.area.name())) {
//                String[] parts = StringUtils.split(vo.getAreaGranular(), ";");
//                List<TemplateGranularityDetailDto> selectList = Stream.of(parts)
//                        .map(v -> tempGra.getDetails().stream().filter(d -> Objects.equals(d.getName(), v)).findAny())
//                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
//                List<String> names = Lambda.buildList(selectList, TemplateGranularityDetailDto::getName);
//                if (selectList.size() != parts.length) {
//                    String missing = Stream.of(parts).filter(v -> !names.contains(v)).collect(Collectors.joining(","));
//                    throw new CustomException("市场信息 异常，粒度未配置: " + missing);
//                }
//
//                List<SelectFieldDto> selectFieldDtos = selectList.stream()
//                        .flatMap(v -> Stream.of(v.getRequired(), v.getOptions()))
//                        .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
//                        .distinct()
//                        .map(v -> SelectFieldDto.builder()
//                                .fieldId(v)
//                                .alias(fieldDictNameMap.get(v))
//                                .build())
//                        .collect(Collectors.toList());
//
//                ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
//                        .granularity(tempGra.getGranularity())
//                        .selectGranularity(names)
//                        .fields(selectFieldDtos)
//                        .build();
//
//                // 市场范围 特殊处理
//                String areaSQL;
//                if (StringUtils.isBlank(vo.getAreaScope())
//                        || vo.getAreaScope().contains("1=1")
//                        || vo.getAreaScope().contains("1 = 1")) {
//                    String area = Stream.of(StringUtils.split(vo.getAreaGranular(), "、"))
//                            .map(v -> "'" + v + "'").collect(Collectors.joining(","));
//                    areaSQL = String.format(" region_type in (%s)", area);
//                } else {
//                    areaSQL = vo.getAreaScope().replace("a.", "");
////                    areaSQL = areaSQL.replace("city_co_name", "city");
//                }
//
//                Optional<FilterDTO> filterOpt = this.parseForFront(areaSQL);
//                if (filterOpt.isPresent()) {
//                    applicationService.fillFieldIdForFilter(transferProperties.getBaseTableId(), filterOpt.get());
//                    tmp.setFilter(filterOpt.get());
//                } else {
//                    throw new CustomException("市场范围解析失败");
//                }
//
//                applyGras.add(tmp);
//            } else {
//                if (CollectionUtils.isNotEmpty(tempGra.getDetails())) {
//                    log.error("未填写粒度 {} 粒度数：{}", tempGra.getGranularity(), tempGra.getDetails().size());
//                }
//            }
//        }
//        info.setGranularity(applyGras);
//        info.setProcessId(template.getProcessId());
//
//        // 指标列
//        List<String> select = vo.parseMetrics();
//        if (CollectionUtils.isNotEmpty(select)) {
//            List<Long> metricsIds = Lambda.buildList(template.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId);
//            List<MetricsDict> metrics = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>()
//                    .lambda().in(MetricsDict::getId, metricsIds));
//
//            Map<String, MetricsDict> metricsDictMap = Lambda.buildMap(metrics, MetricsDict::getFieldName);
//            List<CustomMetricsLabelDto> labels = select.stream().map(v -> {
//                CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
//                MetricsDict dict = metricsDictMap.get(v);
//                if (Objects.isNull(dict)) {
//                    throw new CustomException("指标匹配失败");
//                }
//                dto.setMetricsId(dict.getId());
//                dto.setAlias(dict.getName());
//                dto.setSelect(true);
//                return dto;
//            }).collect(Collectors.toList());
//            info.setCustomMetrics(labels);
//        }
//    }
//
//}
