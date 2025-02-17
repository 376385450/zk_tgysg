package com.sinohealth.system.biz.application.dto;

import lombok.Data;

/**
 * 区间设定信息
 */
@Data
public class ApplicationDistributedSectionInfo {
    /**
     * 区间范围【左】
     */
    private String left;

    /**
     * 区间范围【右】
     */
    private String right;

    /**
     * 是否细化分层
     */
    private Boolean isRefine;

    /**
     * 步长
     */
    private Double stepSize;

    /**
     * 区间名称
     */
    private String name;

    /**
     * 区间描述/区间预览
     */
    private String description;
}
