package com.sinohealth.system.biz.application.dto;

import lombok.Data;

/**
 * 分布信息配置
 */
@Data
public class DistributedInfo {
    /**
     * 分布类型 - 名称
     */
    private String name;

    /**
     * 分布类型字段id
     */
    private Long fieldId;

    /**
     * 分布类型字段列名称
     */
    private String fieldColumnName;

    /**
     * 分布类型字段名称
     */
    private String fieldName;

    /**
     * 分布类型说明
     */
    private String description;
}
