package com.sinohealth.system.biz.application.dto;

import lombok.Data;

import java.util.List;

/**
 * 分布信息配置
 */
@Data
public class ApplicationDistributedInfo {
    /**
     * 分布类型 - 名称
     */
    private String name;

    /**
     * 字段id
     */
    private Long fieldId;

    /**
     * 字段列名称
     */
    private String fieldColumnName;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 区间设定信息
     */
    private List<ApplicationDistributedSectionInfo> sectionInfos;
}
