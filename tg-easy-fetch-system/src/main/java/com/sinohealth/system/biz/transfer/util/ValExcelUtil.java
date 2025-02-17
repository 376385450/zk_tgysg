package com.sinohealth.system.biz.transfer.util;

import com.sinohealth.common.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-08 15:43
 */
public class ValExcelUtil {

    public static final String SPLIT = ";";

    public static List<String> splitMultiple(String cell) {
        if (StringUtils.isBlank(cell)) {
            return Collections.emptyList();
        }
        return Stream.of(cell.split(SPLIT)).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
}
