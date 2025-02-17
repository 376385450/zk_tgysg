package com.sinohealth.common.enums;

/**
 * 数据源
 */
public enum DataSourceType {
    /**
     * 业务主库 MySQL
     */
    MASTER,

    /**
     * CK 内网业务库
     */
    SLAVE,

//    /**
//     * 公网客户PG库
//     */
//    @Deprecated
//    POSTGRESQL,

    POWER_BI_PG,

    /**
     * 公网客户CK库
     */
    CUSTOMER_CK,

    /**
     * hive库
     */
    HIVE;
}
