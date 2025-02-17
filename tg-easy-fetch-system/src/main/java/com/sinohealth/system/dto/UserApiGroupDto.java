package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("接口信息")
public class UserApiGroupDto implements Serializable {

    @ApiModelProperty("接口ID")
    private Long apiId;

    @ApiModelProperty("接口名称")
    private String apiName;

    @ApiModelProperty("分组ID")
    private Long groupId;

    @ApiModelProperty("管理权限")
    private Integer isManage;

    @ApiModelProperty("订阅权限")
    private Integer isSubscribe;
}
