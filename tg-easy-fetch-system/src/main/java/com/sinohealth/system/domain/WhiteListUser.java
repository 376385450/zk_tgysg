package com.sinohealth.system.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WhiteListUser implements Serializable {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("显示名")
    private String viewName;

    @ApiModelProperty("权限标识")
    private List<Integer> authorization;

}
