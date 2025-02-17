package com.sinohealth.system.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("用户api服务管理-编辑对象")
public class UserApiEditVo implements Serializable {

    @ApiModelProperty("当前用户id")
    private Long userId;

    @ApiModelProperty(dataType="List",value = "用户权限")
    private List<UserApiEditInfoVo> userApiEditInfoVos;

}
