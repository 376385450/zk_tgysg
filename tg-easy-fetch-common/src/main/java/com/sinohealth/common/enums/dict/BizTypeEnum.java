package com.sinohealth.common.enums.dict;

import com.google.common.collect.Sets;
import com.sinohealth.common.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 业务线 all 标识全部的业务线
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-11 15:35
 */
public enum BizTypeEnum {
    cmh("CMH", "cmh"),
    small_ticket("小票", "sic"),
    hospital("医院", "yy"),
    O2O("O2O", "o2o"),
    B2C("B2C", "b2c"),
    main_data("主数据", "mas"),
    ;

    /**
     * 参与排期的业务线
     */
    public static final Set<String> PLANS = Sets.newHashSet(cmh.name(), small_ticket.name(), O2O.name(), B2C.name(), hospital.name());

    public static final String ALL = "ALL";

    private final String desc;

    private final String shortId;

    BizTypeEnum(String desc, String shortId) {
        this.desc = desc;
        this.shortId = shortId;
    }

    public String getShortId() {
        return shortId;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(String type) {
        if (Objects.isNull(type)) {
            return "";
        }
        if (Objects.equals(type, ALL)) {
            return "全部";
        }
        for (BizTypeEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return "";
    }

    public static String getDescList(String type) {
        if (Objects.isNull(type)) {
            return "";
        }

        String[] list = type.split(",");
        return Stream.of(list).map(BizTypeEnum::getDesc).collect(Collectors.joining(","));
    }

    public static List<String> splitType(String type) {
        if (StringUtils.isBlank(type)) {
            return Collections.emptyList();
        }
        return Arrays.stream(type.split(",")).collect(Collectors.toList());
    }

}
