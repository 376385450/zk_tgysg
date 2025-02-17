package com.sinohealth.common.enums.monitor;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 1:45 下午
 */
public enum SecondSubjectTypeEnum {

    // --- 地图目录 ---
    MAP_TABLE_VIEW("map_table_view","地图目录-查看表单", SubjectTypeEnum.MAP),

    // --- 地图目录 ---



    // --- 客户资产 ---
    CUSTOMER_APPLY_TABLE_VIEW("customer_table_view", "客户资产-查看表单", SubjectTypeEnum.CUSTOMER),
    CUSTOMER_TABLE_DOWNLOAD_VIEW("customer_table_download", "客户资产-下载表单", SubjectTypeEnum.CUSTOMER),
    CUSTOMER_APPLY_DOWNLOAD_VIEW("customer_apply_download", "客户资产-下载提数", SubjectTypeEnum.CUSTOMER),
    CUSTOMER_APPLY_AUTH_VIEW("customer_auth_view", "客户资产-查看表单", SubjectTypeEnum.CUSTOMER)

    // --- 客户资产 ---

    ;

    private String type;

    private String msg;

    private SubjectTypeEnum subjectType;

    SecondSubjectTypeEnum(String type, String msg, SubjectTypeEnum subjectType) {
        this.type = type;
        this.msg = msg;
        this.subjectType = subjectType;
    }

    public String getType() {
        return type;
    }

    public SubjectTypeEnum getSubjectType() {
        return subjectType;
    }

}
