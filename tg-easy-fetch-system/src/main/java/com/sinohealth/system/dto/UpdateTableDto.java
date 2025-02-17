package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("UpdateTableDto")
public class UpdateTableDto {
    @ApiModelProperty("表ID")
    private Long tableId;
    @ApiModelProperty("表中文")
    private String tableAlias;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("变更类型")
    private String type;
    @ApiModelProperty("最近更新时间")
    private Date updateTime;
}
