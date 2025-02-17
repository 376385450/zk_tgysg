package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表热度枚举
 *
 * @author linkaiwei
 * @date 2022/02/10 16:18
 * @since 1.6.4.0
 */
@Getter
@AllArgsConstructor
public enum HeatEnum {

    HIGH(1, 0, 30, "高热度"),
    MEDIUM(2, 30, 90, "中热度"),
    LOW(3, 90, 365, "低热度"),
    COLD(4, 365, Integer.MAX_VALUE, "冷"),
    ;

    private final int code;
    private final int startDay;
    private final int endDay;
    private final String name;


    public static HeatEnum getInstance(int code) {
        for (HeatEnum heatEnum : values()) {
            if (heatEnum.getCode() == code) {
                return heatEnum;
            }
        }

        return null;
    }

    public static HeatEnum getHeatEnum(int day) {
        for (HeatEnum heatEnum : values()) {
            if (heatEnum.getStartDay() <= day && heatEnum.getEndDay() > day) {
                return heatEnum;
            }
        }

        return null;
    }

}
