package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@ApiModel("编目权限")
@Data
public class UserRightsDTO implements Serializable {

    @ApiModelProperty("部门id")
    private String deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("类型 1员工 2部门")
    private Integer type;

    @ApiModelProperty("可阅读")
    private Integer readable;

    @ApiModelProperty("资产管理")
    private Integer assetsManager;

    @ApiModelProperty("目录管理")
    private Integer catalogueManager;

}
