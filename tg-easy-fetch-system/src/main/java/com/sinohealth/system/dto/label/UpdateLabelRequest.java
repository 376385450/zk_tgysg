package com.sinohealth.system.dto.label;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 18:38
 */
@Data
@ApiModel("更新标签请求参数")
public class UpdateLabelRequest {

    @NotNull(message = "标签ID不能为空")
    @ApiModelProperty("标签ID")
    private Long id;

    @NotBlank(message = "标签名称不能为空")
    @ApiModelProperty("标签名称")
    private String name;
}
