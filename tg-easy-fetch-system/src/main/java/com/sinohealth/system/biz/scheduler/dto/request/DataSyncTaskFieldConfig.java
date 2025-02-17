package com.sinohealth.system.biz.scheduler.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-07-18 10:46
 */
@Data
public class DataSyncTaskFieldConfig implements Serializable {

    private String sourceColumnName;

    private Integer sourceColumnType;

    private String sourceColumnTypeName;

    private String sourceColumnRemark;

    private String targetColumnName;

    private Integer targetColumnType;

    private String targetColumnTypeName;

    private String targetColumnRemark;
}
