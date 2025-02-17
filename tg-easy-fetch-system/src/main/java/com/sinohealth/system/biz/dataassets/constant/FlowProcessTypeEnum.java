package com.sinohealth.system.biz.dataassets.constant;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

/**
 * @author Kuangcp 2024-08-06 15:42
 */
@Getter
public enum FlowProcessTypeEnum {

    qc("QC版本"),
    sop("SOP版本"),
    deliver("交付版本"),
    temp("临时改数版本");

    public static final Set<String> once = Sets.newHashSet(sop.name(), deliver.name());
    /**
     * 数据非标，治理阶段
     */
    public static final Set<String> dev = Sets.newHashSet(qc.name(), sop.name());

    public static final Set<String> rollDelete = Sets.newHashSet(deliver.name(), temp.name());
    public static final Set<String> all = Sets.newHashSet(qc.name(), sop.name(), deliver.name(), temp.name());


    public static boolean isOnceType(String type) {
        return once.contains(type);
    }

    private final String desc;

    FlowProcessTypeEnum(String desc) {
        this.desc = desc;
    }

    public static String getDescByName(String name) {
        for (FlowProcessTypeEnum value : FlowProcessTypeEnum.values()) {
            if (value.name().equals(name)) {
                return value.getDesc();
            }
        }
        return "";
    }

    public static FlowProcessTypeEnum of(String type) {
        for (FlowProcessTypeEnum value : FlowProcessTypeEnum.values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
