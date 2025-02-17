package com.sinohealth.system.biz.application.util;

import com.sinohealth.system.domain.TableFieldInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-05 15:57
 */
@Data
@Builder
public class SelectFieldContext {

    Map<Long, TableFieldInfo> selectFieldMap;
    List<TableFieldInfo> fieldInfos;
    List<Long> metricsQuoteIds;
}
