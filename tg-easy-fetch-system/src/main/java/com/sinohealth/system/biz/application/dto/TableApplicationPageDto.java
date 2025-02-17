package com.sinohealth.system.biz.application.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-05 16:53
 */
@Data
public class TableApplicationPageDto {

    private Long applicationId;
    private Long applicantId;
    private Long baseTableId;
    private String name;
    private Date expireDate;
    private Date applyTime;
    private String applyReason;
    private Integer currentAuditProcessStatus;
}
