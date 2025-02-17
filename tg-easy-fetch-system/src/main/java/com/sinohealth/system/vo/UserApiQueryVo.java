package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("用户api服务管理-查询对象")
public class UserApiQueryVo implements Serializable {

    @ApiModelProperty("接口名称")
    private String apiName;

    @ApiModelProperty("分组类型")
    private Integer groupId;

    @ApiModelProperty("用户id")
    private Integer userId;

    @ApiModelProperty("api状态")
    private Integer apiStatus;
}
