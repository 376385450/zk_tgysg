package com.sinohealth.system.dto.auditprocess;

import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-20 17:22
 * @Desc
 */
@Data
public class AuditApplicationSearchDto {
    private String searchRequireTimeType;
    private String searchClient;
    private String searchReqiureAttr;
    private String searchProcessStatus;
    private String searchProjectName;
    private String searchTableName;
    private String searchContent;
    private String projectName;
    private Integer searchStatus;
    private Integer searchNodeStatus;
    private String searchHandlerName;
    private String searchApplicantName;
    private String applicationNo;
    private String applicationType;
    private Integer isGeneric;
    private Integer auditId;
    private String applyUser;
    // 资产名称
    private String assetName;
    // 排序
    private String order;
    private List<String> extraApplicationType;

    /**
     * 出数状态
     * @see ApplyDataStateEnum
     */
    private String dataState;
}
