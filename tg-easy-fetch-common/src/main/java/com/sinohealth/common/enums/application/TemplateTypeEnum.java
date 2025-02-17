package com.sinohealth.common.enums.application;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-17 11:04
 */
@Getter
public enum TemplateTypeEnum {
    wide_table("宽表"),
    normal("常规"),
    customized("通用"),
    ;

    private final String desc;

    TemplateTypeEnum(String desc) {
        this.desc = desc;
    }

    public static Optional<TemplateTypeEnum> of(String type) {
        if (Objects.isNull(type)) {
            return Optional.empty();
        }
        for (TemplateTypeEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public boolean isSchedulerTaskType() {
        return Objects.equals(this, normal) || Objects.equals(this, customized);
    }

    public static boolean isSchedulerTaskType(String type) {
        return Objects.equals(type, normal.name()) || Objects.equals(type, customized.name());
    }

    public static boolean isNormalType(String type) {
        return Objects.equals(type, normal.name());
    }

    public static boolean isCustomizedType(String type) {
        return Objects.equals(type, customized.name());
    }

    public boolean isNormalType() {
        return Objects.equals(this, normal);
    }

    public boolean isManualTaskType() {
        return Objects.equals(this, customized);
    }

    public boolean isWideTableType() {
        return Objects.equals(this, wide_table);
    }

    public static final List<String> SHARD_PRESET = Arrays.asList(normal.name(), customized.name());

    public static String getDesc(String type) {
        return of(type).map(TemplateTypeEnum::getDesc).orElse("");
    }
}
