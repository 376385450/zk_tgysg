package com.sinohealth.system.biz.dataassets.util;

import com.sinohealth.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kuangcp
 * 2024-11-01 13:55
 */
public class AssetsFlowAutoBatchUtil {

    public static List<String> parse(String string) {
        if (StringUtils.isBlank(string)) {
            return Collections.emptyList();
        }
        return Stream.of(string.split("#")).collect(Collectors.toList());
    }

    public static boolean containSearch(String string) {
        return StringUtils.isNotBlank(string) && string.contains("|");
    }

    public static List<String> parseSearch(String string) {
        if (StringUtils.isBlank(string)) {
            return Collections.emptyList();
        }
        return Stream.of(string.split("\\|")).collect(Collectors.toList());
    }

    public static List<Integer> parseInt(String string) {
        if (StringUtils.isBlank(string)) {
            return Collections.emptyList();
        }
        return Stream.of(string.split("#")).map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Long> parseLong(String string) {
        if (StringUtils.isBlank(string)) {
            return Collections.emptyList();
        }
        return Stream.of(string.split("#")).map(Long::parseLong).collect(Collectors.toList());
    }

    public static <T> String buildStr(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return list.stream().map(Object::toString).collect(Collectors.joining("#"));
    }
}
