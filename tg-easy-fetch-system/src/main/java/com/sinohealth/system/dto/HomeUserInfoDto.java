package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@ApiModel
public class HomeUserInfoDto implements Serializable {

    @ApiModelProperty(value = "当前用户token")
    private String token;
    @ApiModelProperty(value = "当前用户名称")
    private String currentUserName;
    @ApiModelProperty(value = "当前用户真实名称")
    private String currentRealName;
    @ApiModelProperty(value = "当前用户角色")
    private Set<String> currentRoles;
    @ApiModelProperty(value = "当前用户所在组")
    private Set<String> currentGroups;

    @ApiModelProperty(value = "当前用户登录次数")
    private Integer currentLoginNum;

    @ApiModelProperty(value = "当前用户数据权限")
    private Long currentDataNum;

    @ApiModelProperty(value = "运行任务")
    private Integer runningCnt;

    @ApiModelProperty(value = "运行成功")
    private Integer sucessCnt;

    @ApiModelProperty(value = "运行失败")
    private Integer failCnt;


}
