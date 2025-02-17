package com.sinohealth.system.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-15 15:11
 * @Desc
 */
@ApiModel(description = "标记部门或人员选择项")
@Data
public class TgAssetStaffParam {
    @ApiModelProperty("1-部门/2-人员")
    private Integer type;
    @ApiModelProperty("部门id/人员id")
    private String id;
    @ApiModelProperty("部门名/人员名")
    private String name;
    @ApiModelProperty("资产开放服务")
    private List<String> assetOpenServices = new ArrayList<>();
    @ApiModelProperty("有效期")
    private Date expirationDate;
}

