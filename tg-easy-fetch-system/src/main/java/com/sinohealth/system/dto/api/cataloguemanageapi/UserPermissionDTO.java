package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@Data
@ApiModel("用户权限")
public class UserPermissionDTO implements Serializable {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String realName;

    @ApiModelProperty("部门id")
    private String deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("类型 2.员工 1.部门")
    private Integer type;

    @ApiModelProperty("可阅读")
    private Integer readable;

    @ApiModelProperty("资产管理")
    private Integer assetsManager;

    @ApiModelProperty("目录管理")
    private Integer catalogueManager;

}
