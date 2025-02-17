package com.sinohealth.common.enums;

import java.util.Locale;

/**
 * 数据集类型枚举
 *
 * @author linkaiwei
 * @date 2021/9/27 14:33
 * @since 1.4.3.0
 */
public enum DatasetTypeEnum {

    CUSTOMIZE(1, "自定义数据集"),
    EXCEL(2, "EXCEL数据集"),
    ;

    private final int type;
    private final String desc;


    DatasetTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据数据集类型，获取数据集类型枚举
     *
     * @param type 数据集类型
     * @return 数据集类型枚举
     * @author linkaiwei
     * @date 2021-09-27 14:39:00
     * @since 1.4.3.0
     */
    public static DatasetTypeEnum getDatasetTypeByDesc(int type) {
        for (DatasetTypeEnum datasetTypeEnum : DatasetTypeEnum.values()) {
            if (datasetTypeEnum.type == type) {
                return datasetTypeEnum;
            }
        }
        return null;
    }

    /**
     * 根据类型描述，获取数据集类型枚举
     *
     * @param desc 类型描述（支持模糊搜索）
     * @return 数据集类型枚举
     * @author linkaiwei
     * @date 2021-09-27 14:39:00
     * @since 1.4.3.0
     */
    public static DatasetTypeEnum getDatasetTypeByDesc(String desc) {
        if (desc != null && desc.trim().length() > 0) {
            for (DatasetTypeEnum datasetTypeEnum : DatasetTypeEnum.values()) {
                if (datasetTypeEnum.desc.contains(desc.toUpperCase(Locale.ROOT))) {
                    return datasetTypeEnum;
                }
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
