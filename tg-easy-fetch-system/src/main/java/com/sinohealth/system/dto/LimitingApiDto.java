package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class LimitingApiDto implements Serializable {
    @ApiModelProperty(value = "apiID")
    private Long apiId;

    @ApiModelProperty(value = "服务中文名")
    private String apiName;

    @ApiModelProperty(value = "服务英文名")
    private String apiNameEn;

    @ApiModelProperty(value = "接口访问路径")
    private String requestPath;

    @ApiModelProperty(value = "接口所属自定义组", hidden = true)
    private Long groupId;

    @ApiModelProperty(value = "接口所属自定义组")
    private String groupName;
}
