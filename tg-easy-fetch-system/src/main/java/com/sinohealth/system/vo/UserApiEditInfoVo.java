package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("用户api服务管理-编辑对象")
public class UserApiEditInfoVo implements Serializable {

    @ApiModelProperty("接口ID")
    private Long apiId;

    @ApiModelProperty("是否管理(0否|1是)")
    private Integer isManage;

    @ApiModelProperty("是否订阅(0否|1是)")
    private Integer isSubscribe;
}
