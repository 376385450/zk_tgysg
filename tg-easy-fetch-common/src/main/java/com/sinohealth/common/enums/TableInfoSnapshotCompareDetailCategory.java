package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @author monster
 * @Date 2024-07-12 15:52
 */
public enum TableInfoSnapshotCompareDetailCategory {
    /**
     * ck|hive同步表
     */
    HDFS_CK_HIVE("hdfs_ck_hive", "ck|hive同步表"),

    /**
     * ck|hive比对表
     */
    HIVE_DIFF("hive_diff", "hive比对表"),

    /**
     * ck|比对宽表
     */
    DIFF_WIDE("diff_wide", "比对宽表"),

    /**
     * ck变化明细临时表
     */
    DIFF_DETAIL_TEMP("diff_detail_temp", "变化明细临时表"),

    /**
     * ck比对宽表额外信息暂存临时表
     */
    DIFF_WIDE_EXTRA_TEMP("diff_wide_extra_temp", "比对宽表额外信息暂存表"),

    /**
     * ck&hive变化明细表
     */
    DIFF_DETAIL("diff_detail", "变化明细表"),

    /**
     * ck&hive比对宽表额外信息暂存表
     */
    DIFF_WIDE_EXTRA("diff_wide_extra", "比对宽表额外信息暂存表");

    @Getter
    private String type;

    @Getter
    private String describe;

    TableInfoSnapshotCompareDetailCategory(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }
}
