package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author Jingjun
 * @since 2021/4/25
 */
@Data
@ApiModel("TableBaseInfoDto")
public class TableBaseInfoDto {

    private String tableName;

    @ApiModelProperty("数据最近更新时间")
    private Date updateTime;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("行数")
    private Long totalRows;

    @ApiModelProperty("字段数量")
    private Long totalFields;

    @ApiModelProperty("文件大小")
    private Long storeSize;

    private String comment;
}
