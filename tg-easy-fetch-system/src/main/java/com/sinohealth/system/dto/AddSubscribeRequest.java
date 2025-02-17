package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("api订阅")
public class AddSubscribeRequest {

    @ApiModelProperty(value = "api发布后的接口ID，逗号分隔如 1，2，3")
    @NotBlank(message = "apiVersionIds 不能为 null")
    private String apiVersionIds;

    @ApiModelProperty(value = "申请订阅原因")
    @NotBlank(message = "subscribeReason 不能为 null")
    private String subscribeReason;


}
