package com.sinohealth.system.dto.label;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 17:57
 */
@Data
@ApiModel("新增标签请求参数")
@NoArgsConstructor
@AllArgsConstructor
public class AddLabelRequest {

    @NotBlank(message = "标签名称不能为空")
    @ApiModelProperty("标签名称")
    private String name;
}
