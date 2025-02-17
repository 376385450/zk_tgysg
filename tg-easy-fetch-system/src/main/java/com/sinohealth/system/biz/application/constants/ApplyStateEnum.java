package com.sinohealth.system.biz.application.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 需求单总状态
 *
 * @author Kuangcp
 * 2024-12-10 13:41
 */
@Getter
public enum ApplyStateEnum {
    /**
     * 未审核通过时 未生成
     */
    none("未生成"),
    /**
     * 可使用
     */
    normal("可使用"),
    /**
     * 已暂停 可使用的子状态
     */
    pause("已暂停"),
    /**
     * 已过期
     */
    expire("已过期"),
    /**
     * 已作废
     */
    deprecated("已作废");

    public static final List<String> WATCH_EXPIRE = Arrays.asList(none.name(), normal.name(), pause.name());

    private final String desc;

    ApplyStateEnum(String desc) {
        this.desc = desc;
    }

    public static String getDesc(String type) {
        for (ApplyStateEnum value : values()) {
            if (value.name().equals(type)) {
                return value.desc;
            }
        }
        return "";
    }

}
