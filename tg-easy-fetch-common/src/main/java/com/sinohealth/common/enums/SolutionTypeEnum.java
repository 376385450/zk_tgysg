package com.sinohealth.common.enums;

/**
 * 分析方案类型枚举
 *
 * @author linkaiwei
 * @date 2021/8/19 14:16
 * @since 1.4.1.0
 */
public enum SolutionTypeEnum {

    VISUAL("可视化图表分析方案"),
    ZERO("零代码分析方案"),
    SQL("SQL代码分析方案"),
    PYTHON3("PYTHON3代码分析方案"),
    ;

    private final String desc;

    SolutionTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 获取枚举
     *
     * @param type 枚举名称
     * @return 枚举
     * @author linkaiwei
     * @date 2021-08-19 14:25:04
     * @since 1.4.2.0
     */
    public static SolutionTypeEnum getSolutionTypeEnum(String type) {
        if (type != null && type.trim().length() > 0) {
            for (SolutionTypeEnum solutionTypeEnum : SolutionTypeEnum.values()) {
                if (solutionTypeEnum.name().equalsIgnoreCase(type)) {
                    return solutionTypeEnum;
                }
            }
        }

        return null;
    }
}
