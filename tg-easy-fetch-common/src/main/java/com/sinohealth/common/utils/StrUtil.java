package com.sinohealth.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-20 18:42
 */
@Slf4j
public class StrUtil {

    private static final String DICT = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
    private static final int length = DICT.length();

    public static String firstNotBlankStr(String... names) {
        return Stream.of(names).filter(StringUtils::isNotBlank).findFirst().orElse("");
    }

    public static String randomAlpha(int len) {
        Random random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(DICT.charAt(random.nextInt(length)));
        }
        return builder.toString();
    }

    public static String decode(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (!str.contains("%")) {
            return str;
        }

        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
            return str;
        }
    }

    public static List<String> split(String content) {
        if (Objects.isNull(content)) {
            return Collections.emptyList();
        }
        return Arrays.stream(content.split(",")).collect(Collectors.toList());
    }

}
