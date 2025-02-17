package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("接口信息")
public class UserApiDto implements Serializable {

    @ApiModelProperty("接口id")
    private Long apiId;

    @ApiModelProperty("URL地址")
    private String pubPath;

    @ApiModelProperty("接口英文名")
    private String apiEName;

    @ApiModelProperty("接口名称")
    private String apiName;

    @ApiModelProperty("接口版本")
    private String apiVersion;

    @ApiModelProperty("所属分类")
    private String groupName;

    @ApiModelProperty("所属分类ID")
    private Long groupId;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("管理权限")
    private Integer isManage;

    @ApiModelProperty("订阅权限")
    private Integer isSubscribe;

}
