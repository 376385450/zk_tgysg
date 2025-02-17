package com.sinohealth.system.biz.application.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Kuangcp
 * 2024-12-11 10:45
 */
@Data
public class ApplicationFormPageDto {

    private Integer no;

    private Long applicationId;

    private String createTime;

    private Integer auditState;

    @ApiModelProperty("出数人")
    private String handleUser;

    private String auditTime;

    private Integer assetsVersion;

    private String projectName;
}
