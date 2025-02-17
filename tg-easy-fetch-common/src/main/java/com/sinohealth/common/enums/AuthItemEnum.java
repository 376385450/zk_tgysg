package com.sinohealth.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AuthItemEnum {

    FOLLOW_DIR_AUTH("FOLLOW_DIR_AUTH", "跟随目录"),

    CUSTOM_AUTH("CUSTOM_AUTH", "自定义");

    private String type;
    @EnumValue
    private String describe;

    AuthItemEnum(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }
}
