package com.sinohealth.system.domain.value.deliver.util;

import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Predicate;

/**
 * 导出文件时 隐藏 字段
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-04-21 18:32
 */
@Slf4j
public class HiddenFieldUtils {

    public static final Predicate<String> APPLY_PREDICATE = v ->
            !Objects.equals(v, ApplicationConst.PeriodField.PERIOD_NEW)
                    && !Objects.equals(v, ApplicationConst.PeriodField.PERIOD_NEW_ALIAS);

    public static final Predicate<AuthTableFieldDTO> CUSTOMER_PREDICATE = v ->
            !Objects.equals(v.getFieldName(), ApplicationConst.PeriodField.PERIOD_NEW)
                    && !Objects.equals(v.getFieldName(), ApplicationConst.PeriodField.PERIOD_NEW_ALIAS);

    /**
     * 对客户，只有字段英文名
     */
    public static List<Object> hiddenForCustomer(String[] fieldHeader, List<LinkedHashMap<String, Object>> dataMaps, Object defaultVal) {
        List<Object> lines = TgCollectionUtils.newArrayList();

        for (LinkedHashMap<String, Object> d : dataMaps) {
            List<Object> row = new ArrayList<>();
            for (String field : fieldHeader) {
                // 简单处理 不支持的类型 https://github.com/alibaba/easyexcel/issues/980
                Object val = d.getOrDefault(field, defaultVal);
                if (val instanceof Timestamp) {
                    row.add(new Date(((Timestamp) val).getTime()));
                } else if (val instanceof java.sql.Date) {
                    row.add(new Date(((java.sql.Date) val).getTime()));
                } else {
                    row.add(val);
                }
            }
            lines.add(row);
        }
        return lines;
    }

    /**
     * 内部，原始SQL可得到字段英文名，别名的关系
     */
    public static List<List<Object>> matchLines(String[] headers,
                                                List<LinkedHashMap<String, Object>> dataMaps,
                                                Object defaultVal) {
        List<List<Object>> lines = new ArrayList<>(dataMaps.size());
        Set<String> notMatchFields = new HashSet<>();
        for (LinkedHashMap<String, Object> d : dataMaps) {
            List<Object> row = new ArrayList<>(headers.length);
            for (String field : headers) {
                Object val = d.get(field);
                // 值的列名超出了select的列名 不匹配 产生NULL值时日志打印， 正常NULL值时忽略
                if (Objects.isNull(val)) {
                    if (notMatchFields.add(field) && !d.containsKey(field)) {
                        log.warn("NOT match: field={} header={} valKey={}", field, headers, d.keySet());
                    }
                    row.add(defaultVal);
                } else if (val instanceof Integer || val instanceof Double || val instanceof Float
                        || val instanceof Long || val instanceof BigDecimal || val instanceof Decimal) {
                    row.add(val);
                } else {
                    row.add(val.toString());
                }
            }
            lines.add(row);
        }
        return lines;
    }

    public static List<List<Object>> matchLinesWithProject(String[] headers,
                                                           List<LinkedHashMap<String, Object>> dataMaps,
                                                           Object defaultVal, boolean needExportName, String projectName) {
        List<List<Object>> lines = new ArrayList<>(dataMaps.size());
        Set<String> notMatchFields = new HashSet<>();
        for (LinkedHashMap<String, Object> d : dataMaps) {
            List<Object> row = new ArrayList<>(headers.length + 1);
            if (needExportName) {
                row.add(projectName);
            }
            for (String field : headers) {
                Object val = d.get(field);
                // 值的列名超出了select的列名 不匹配 产生NULL值时日志打印， 正常NULL值时忽略
                if (Objects.isNull(val)) {
                    if (notMatchFields.add(field) && !d.containsKey(field)) {
                        log.warn("NOT match: field={} header={} valKey={}", field, headers, d.keySet());
                    }
                    row.add(defaultVal);
                } else if (val instanceof Integer || val instanceof Double || val instanceof Float
                        || val instanceof Long || val instanceof BigDecimal || val instanceof Decimal) {
                    row.add(val);
                } else {
                    row.add(val.toString());
                }
            }
            lines.add(row);
        }
        return lines;
    }


}
