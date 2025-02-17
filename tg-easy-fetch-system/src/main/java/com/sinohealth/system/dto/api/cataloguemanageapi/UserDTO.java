package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/11
 */
@Data
@ApiModel("用户")
public class UserDTO implements Serializable {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名称")
    private String name;

    @ApiModelProperty("部门信息")
    private String deptInfo;

    @ApiModelProperty("部门全路径")
    private String deptFullPath;

}
