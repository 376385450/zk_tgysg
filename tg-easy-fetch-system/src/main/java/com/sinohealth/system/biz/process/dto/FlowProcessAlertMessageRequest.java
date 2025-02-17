package com.sinohealth.system.biz.process.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FlowProcessAlertMessageRequest {
    @ApiModelProperty(name = "期数")
    private String period;

    @ApiModelProperty(name = "版本类型")
    private String versionCategory;

    @ApiModelProperty(name = "流程名称")
    private String manageName;

    @ApiModelProperty(name = "关联品类")
    private String productCodes;
}
