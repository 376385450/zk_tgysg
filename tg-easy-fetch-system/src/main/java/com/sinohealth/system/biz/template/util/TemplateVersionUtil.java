package com.sinohealth.system.biz.template.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-22 14:43
 */
@Slf4j
public class TemplateVersionUtil {

    /**
     * 如果业务没有发生修改，不进行版本迭代
     *
     * 需求链接： https://lanhuapp.com/web/#/item/project/product?tid=641198dd-0c9d-4c69-852a-2ebc9c98ff03&pid=6613af4a-cd93-4007-a3ff-6577c9eae382&image_id=53eb1662-8959-46b3-88f0-0e18d7ece64d&docId=53eb1662-8959-46b3-88f0-0e18d7ece64d&docType=axure&versionId=dd68782f-7963-4fc2-bde0-ee28cf4a6e4d&pageId=d9c2a76381f549c389afe4ab90e2d65e&parentId=6638819432274427bd9720d3188b59e6
     * 基础：业务线，模板类型
     * 宽表：
     * 常规/通用：
     *
     * @return 差异字段信息
     */
    public static List<String> needSaveSnapshot(TgTemplateInfo exist, TgTemplateInfo request) {
        JsonBeanConverter.convert2Json(exist);
        JsonBeanConverter.convert2Json(request);

        Map<String, Function<TgTemplateInfo, Object>> diffFunc = new HashMap<>();
        diffFunc.put("模式设置", TgTemplateInfo::getColAttr);
        diffFunc.put("关联设置-新增关联关系", TgTemplateInfo::getJoinTableAttr);
        diffFunc.put("表单设置-当前表单", TgTemplateInfo::getBaseTableId);
        diffFunc.put("表单设置-关联关系", TgTemplateInfo::getJoinJson);
        diffFunc.put("数据列设置-数据列", TgTemplateInfo::getColsJson);
        diffFunc.put("指标列设置", TgTemplateInfo::getMetricsJson);
//        diffFunc.put("数据范围设置", TgTemplateInfo::getDataRangeJson);
        diffFunc.put("时间分组设置", TgTemplateInfo::getApplicationPeriodFieldJson);
        diffFunc.put("自选维度-必填字段", TgTemplateInfo::getMustSelectFieldsJson);

        addGranularityFunc(diffFunc, "时间信息", FieldGranularityEnum.time);
        addGranularityFunc(diffFunc, "市场信息", FieldGranularityEnum.area);
        addGranularityFunc(diffFunc, "产品信息", FieldGranularityEnum.product);
        addGranularityFunc(diffFunc, "会员信息", FieldGranularityEnum.member);
        addGranularityFunc(diffFunc, "其他信息", FieldGranularityEnum.other);

        // 1.9.1 选中状态修改不更新版本
        diffFunc.put("指标列设置", a -> {
            String json = a.getCustomMetricsJson();
            if (StringUtils.isBlank(json)) {
                return null;
            }
            List<CustomMetricsLabelDto> list = Optional.ofNullable(JsonUtils.parseArray(json, CustomMetricsLabelDto.class))
                    .orElse(new ArrayList<>());
            List<CustomMetricsLabelDto> mod = list.stream()
                    .sorted(Comparator.comparing(CustomMetricsLabelDto::getMetricsId))
                    .peek(v -> v.setSelect(null)).collect(Collectors.toList());
            return JsonUtils.format(mod);
        });


        diffFunc.put("Top设置", TgTemplateInfo::getTopSettingJson);

//        diffFunc.put("SQL查询方式", TgTemplateInfo::getSqlBuildMode);
        diffFunc.put("模板类型", TgTemplateInfo::getTemplateType);
        diffFunc.put("业务类型", TgTemplateInfo::getBizType);

        diffFunc.put("其他设置-粒度选择", TgTemplateInfo::getCustomGranularity);

//        diffFunc.put("其他设置", TgTemplateInfo::getCustomExt);
        diffFunc.put("其他设置", a -> {
            String ext = a.getCustomExt();
            if (StringUtils.isBlank(ext)) {
                return null;
            }
            JSONArray params = JSON.parseArray(ext);
            for (Object param : params) {
                JSONObject obj = (JSONObject) param;
                obj.remove("note");
                obj.remove("tips");
            }
            return params.toJSONString();
        });


        return diffFunc.entrySet().stream().map(v -> {
            Function<TgTemplateInfo, Object> func = v.getValue();
            Object a = func.apply(exist);
            Object b = func.apply(request);
            if (Objects.isNull(a) && Objects.isNull(b)) {
                return null;
            }
            if (Objects.isNull(b)) {
                log.debug("ignore null request: {}", v.getKey());
                return null;
            }

            boolean result = !Objects.equals(a, b);
            if (result) {
                log.info("diff field: {} exist:{} request:{}", v.getKey(), a, b);
                return v.getKey();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static void addGranularityFunc(Map<String, Function<TgTemplateInfo, Object>> diffFunc, String tips, FieldGranularityEnum type) {
        diffFunc.put(tips, a -> {
            String json = a.getGranularityJson();
            if (StringUtils.isBlank(json)) {
                return null;
            }
            List<TemplateGranularityDto> list = Optional.ofNullable(JsonUtils.parseArray(json, TemplateGranularityDto.class)).orElse(new ArrayList<>());
            for (TemplateGranularityDto dto : list) {
                dto.setGranularityRequired(null);
                dto.setFilterRequired(null);
                dto.setTips(null);
                dto.setRangeTemplateTips(null);
                dto.setRequired(null);
                if (CollectionUtils.isNotEmpty(dto.getDetails())) {
                    for (TemplateGranularityDetailDto detail : dto.getDetails()) {
                        distinctAndSort(detail::getRequired, detail::setRequired);
                        distinctAndSort(detail::getOptions, detail::setOptions);
                    }
                }
            }

            return list.stream().filter(v -> Objects.equals(v.getGranularity(), type.name()))
                    .map(JsonUtils::format).filter(Objects::nonNull).findAny().orElse(null);
        });
    }

    private static void distinctAndSort(Supplier<List<Long>> a, Consumer<List<Long>> b) {
        List<Long> val = a.get();
        if (CollectionUtils.isEmpty(val)) {
            return;
        }

        List<Long> sorted = val.stream().distinct().sorted().collect(Collectors.toList());
        b.accept(sorted);
    }

}
