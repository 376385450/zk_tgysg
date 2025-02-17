package com.sinohealth.system.domain.value.deliver;


/**
 * 数据来源
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-25 13:43
 */
public interface DataSource {

    /**
     * 数据源标识
     * @return
     */
    Long getId();

    /**
     * 数据源名称
     * @return
     */
    String getName();

    /**
     * 数据源类型
     * @return
     */
    DeliverDataSourceType support();
}
