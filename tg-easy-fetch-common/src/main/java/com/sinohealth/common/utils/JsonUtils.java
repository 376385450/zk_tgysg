package com.sinohealth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * JSON工具类
 *
 * @author linkaiwei
 * @date 2021/9/3 10:05
 * @since 1.4.2.0
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    private JsonUtils() {
    }


    public static String format(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }

        try {
            return JACKSON_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("", e);
            return null;
        }
    }

    public static <T> T parse(String jsonStr, Class<T> objClass) {
        if (jsonStr == null) {
            return null;
        }
        try {
            return JACKSON_MAPPER.readValue(jsonStr, objClass);
        } catch (JsonProcessingException e) {
            log.error("", e);
            return null;
        }
    }

    public static <T> T parse(String jsonStr, TypeReference<T> objClass) {
        if (jsonStr == null) {
            return null;
        }
        try {
            return JACKSON_MAPPER.readValue(jsonStr, objClass);

        } catch (JsonProcessingException e) {
            log.error("", e);
            return null;
        }
    }

    public static <T> List<T> parseArray(String jsonStr, Class<T> objClass) {
        if (jsonStr == null) {
            jsonStr = "[]";
        }
        try {
            final JavaType javaType = JACKSON_MAPPER.getTypeFactory().constructParametricType(List.class, objClass);
            return JACKSON_MAPPER.readValue(jsonStr, javaType);

        } catch (JsonProcessingException e) {
            log.error("", e);
            return null;
        }
    }

}
