package com.sinohealth.system.biz.application.entity;

import lombok.Data;

/**
 * @author Kuangcp
 * 2024-11-12 18:44
 */
@Data
public class HistoryApplyQuoteEntity {

    private Long id;

    private String applicationNo;

    private Long projectId;

    private Long templateId;

    private String applyName;

    private String projectName;

    private String bizType;

    private String templateName;

    private Integer currentAuditProcessStatus;

    private String createTime;

    private String applicant;
}
