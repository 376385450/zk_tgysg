package com.sinohealth.system.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-05-12 14:56
 * @Desc
 */
@ApiModel(description = "指标信息")
@Data
public class MetricsInfoDto {
    @ApiModelProperty("表ID")
    private Long tableId;
    @ApiModelProperty("表名称")
    private String tableName;
    @ApiModelProperty("拷贝字段名称")
    private String copyfieldName;

    /**
     * 表的字段id
     * <p>
     * 前端传入的是字段库id，中间做了映射转换，拼凑SQL时还是用的表字段id
     */
    @ApiModelProperty("字段id")
    private Long colName;
    @ApiModelProperty("计算方式")
    private Integer computeWay;
    @ApiModelProperty("计算内容")
    private String content;
    @ApiModelProperty("自定义计算SQL")
    private String expressionContent;
    @ApiModelProperty("自用字段,指标内容")
    @JsonIgnore
    private String metricsContent;
    @ApiModelProperty("自用字段,数据范围内容")
    @JsonIgnore
    private String dataRangeContent;
    @ApiModelProperty("是否选中")
    private Integer isConditions;
    @ApiModelProperty("数据范围")
    private String dataRange;
    @ApiModelProperty("条件表达式")
    private Integer conditions;
    @ApiModelProperty("别名")
    private String aliasName;
    /**
     * @see CommonConstants#TEMPLATE
     */
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself = 2;
    @ApiModelProperty("是否全选, 2为全选")
    @Deprecated
    private Integer isAllSelected = 2;

    private Integer length;
    @ApiModelProperty("字段类型")
    private String type;
}
