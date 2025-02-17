package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("TgCustomerApplyAuthDto")
public class TgCustomerApplyAuthDto {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("数据资产id")
    private Long assetsId;

    @ApiModelProperty("客户id")
    private Long userId;

    @ApiModelProperty("报表权限：1:查看;2下载")
    private String authType;

    @ApiModelProperty("更新人Id")
    private Long updateId;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态：1正常，2禁用")
    private Integer status;

    @ApiModelProperty("对外报表名")
    private String outTableName;

    @ApiModelProperty("客户简称")
    private String customer;

    @ApiModelProperty("更新人带组织架构")
    private String updateByOri;

    @ApiModelProperty("使用客户")
    private String fullName;

    @ApiModelProperty("来源表ID")
    private Long baseTableId;

    @ApiModelProperty("来源表")
    private String tableName;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("数据量")
    private Long dataTotal;

    @ApiModelProperty("导出次数")
    private Integer exportTotal;


}
