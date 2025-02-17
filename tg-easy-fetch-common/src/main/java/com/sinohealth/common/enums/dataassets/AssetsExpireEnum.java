package com.sinohealth.common.enums.dataassets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 14:29
 */
public enum AssetsExpireEnum {
    /**
     * 正常 有效
     */
    normal(1),
    /**
     * 过期，但资产可预览，BI视图软删除
     */
    expire(2),
    /**
     * 过期，资产不可预览，准备被删除
     */
    delete(3),
    /**
     * 过期，资产不可预览，已经被删除 连带BI视图硬删除
     */
    delete_data(4),
    ;

    private final int code;

    public int getCode() {
        return code;
    }

    AssetsExpireEnum(int code) {
        this.code = code;
    }

    /**
     * 表存在
     */
    public static final List<String> TABLE_EXIST = Arrays.asList(normal.name(), expire.name());

    public static final List<String> DELETE_TAGS = Arrays.asList(delete.name(), delete_data.name());

    public static boolean isDelete(String type) {
        return Objects.equals(type, AssetsExpireEnum.delete.name()) || Objects.equals(type, AssetsExpireEnum.delete_data.name());
    }
}
