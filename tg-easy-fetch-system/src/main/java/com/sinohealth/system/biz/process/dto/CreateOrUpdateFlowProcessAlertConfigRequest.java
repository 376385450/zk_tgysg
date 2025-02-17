package com.sinohealth.system.biz.process.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建或更新全流程告警信息
 */

@Setter
@Getter
@ToString
public class CreateOrUpdateFlowProcessAlertConfigRequest {
    @ApiModelProperty(value = "详细信息")
    @Valid
    @NotNull(message = "配置详细信息不可为空")
    private List<CreateOrUpdateFlowProcessAlertConfigDetailRequest> details;
}
