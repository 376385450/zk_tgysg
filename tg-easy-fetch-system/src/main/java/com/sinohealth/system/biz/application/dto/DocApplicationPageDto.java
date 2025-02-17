package com.sinohealth.system.biz.application.dto;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-12-23 11:31
 * @Desc
 */
@Data
public class DocApplicationPageDto {
    private Long applicationId;
    private Long applicantId;
    private String docName;
    private String applyComment;
    private String applyTime;
    private String applicationType;
    private Integer currentAuditProcessStatus;
    private String docNameBak;
}
