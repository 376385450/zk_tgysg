package com.sinohealth.system.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-02-06 14:48
 */
public class ListUtil {

    public static <T> List<T> toList(String value, Function<String, T> function) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).filter(StringUtils::isNoneBlank).map(String::trim)
                .map(function).collect(Collectors.toList());
    }

}
