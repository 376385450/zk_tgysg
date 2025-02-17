package com.sinohealth.system.biz.application.dto;

import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import lombok.Data;

import java.util.Date;

/**
 * @Author Rudolph
 * @Date 2022-05-23 16:06
 * @Desc
 */

@Data
public class DataApplicationPageDto {
    private Long applicationId;
    private Long applicantId;
    private Long baseTableId;
    private String tableAlias;
    private String baseTableName;
    /**
     * 需求名
     */
    private String projectName;
    /**
     * 项目名
     */
    private String newProjectName;
    private String templateName;
    private Integer requireAttr;
    private String readableUsers;
    private String requireTimeType;
    private String applicationType;
    private Date dataExpir;
    private Date applyTime;
    private String applyType;
    private String applyReason;
    private Integer currentAuditProcessStatus;

    private Boolean relateAssets;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;

}
