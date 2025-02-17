package com.sinohealth.system.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-12 14:44
 * @Desc
 */
@ApiModel(description = "维度信息")
@Data
public class ColsInfoDto {
    @ApiModelProperty("表ID")
    private Long tableId;
    @ApiModelProperty("表名")
    private String tableName;

    /**
     * 前端使用，回显全部字段
     */
    @ApiModelProperty("原始列id集合")
    private List<Long> copySelect;

    @ApiModelProperty("选中的列ID")
    private List<Long> select;

    @ApiModelProperty("对外名称信息")
    private List<RealName> realName;
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself;

    @ApiModelProperty("前端组件使用 该表维度是否全选")
    private Boolean isAllState;
}
