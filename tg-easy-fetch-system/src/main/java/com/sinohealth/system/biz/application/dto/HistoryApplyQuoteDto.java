package com.sinohealth.system.biz.application.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-21 11:41
 */
@Data
@Builder
public class HistoryApplyQuoteDto implements Serializable {
    private Long id;
    @ApiModelProperty("需求Id")
    private String applicationNo;
    private String projectName;
    private String createTime;
    @ApiModelProperty("流程状态")
    private Integer currentAuditProcessStatus;

    private String bizType;
    private String templateName;
    private String applicant;

}
