package com.sinohealth.system.biz.process.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Setter
@Getter
@ToString
public class FlowProcessSaveSettingRequest {
    @ApiModelProperty(value = "流程名称")
    @Size(max = 64, message = "流程名称不可超过64位字符")
    private String name;

    @ApiModelProperty(value = "计划执行时间")
    @NotBlank(message = "计划执行时间不可为空")
    private String planExecutionTime;

    @ApiModelProperty(value = "业务线")
    @NotBlank(message = "业务线不可为空")
    private String bizType;

    @ApiModelProperty(value = "底表资产编号")
    @NotNull(message = "底表不可为空")
    private Long tableAssetId;

    @ApiModelProperty(value = "工作流模板资产编号")
    @NotNull(message = "工作流模板不可为空")
    private List<Long> modelAssetIds;

    /**
     * @see com.sinohealth.common.enums.process.FlowProcessCategory
     */
    @ApiModelProperty(value = "配置类型【手动、自动】")
    @NotBlank(message = "配置类型不可为空")
    private String category;

    @ApiModelProperty(value = "详细信息")
    @NotNull(message = "配置信息不可为空")
    @Valid
    private List<FlowProcessSaveSettingDetailRequest> details;
}
