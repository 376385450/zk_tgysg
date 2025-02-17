package com.sinohealth.system.dto.label;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 18:49
 */
@Data
@ApiModel("删除标签的请求参数")
public class DeleteLabelRequest {

    @NotNull(message = "标签ID不能为空")
    @ApiModelProperty("标签ID")
    private Long id;
}
