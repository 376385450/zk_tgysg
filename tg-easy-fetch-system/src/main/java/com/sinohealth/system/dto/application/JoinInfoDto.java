package com.sinohealth.system.dto.application;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @Author Rudolph
 * @Date 2022-05-12 14:56
 * @Desc
 */

@ApiModel(description = "关联信息")
@Data
public class JoinInfoDto {
    @ApiModelProperty("表1ID")
    private Long tableId1;
    @ApiModelProperty("表1名称")
    @Size(max = 100, message = "表名长度超出限制")
    private String tableName1;
    @JsonIgnore
    private String tableDistributeName1;
    @JsonIgnore
    private String tableDistributeName2;
    @ApiModelProperty("表1关联字段")
    private Long joinCol1;
    @ApiModelProperty("表1关联字段名")
    private String joinColName1;
    @ApiModelProperty("表2ID")
    private Long tableId2;
    @ApiModelProperty("表2关联字段")
    private Long joinCol2;
    @ApiModelProperty("表2名称")
    @Size(max = 100, message = "表名长度超出限制")
    private String tableName2;
    @ApiModelProperty("表2关联字段名")
    private String joinColName2;
    @ApiModelProperty("关联类型")
    private Integer joinType;
    @ApiModelProperty("自用逻辑字段")
    @JsonIgnore
    @TableField(exist = false)
    private String joinContent;
    /**
     * @see CommonConstants#TEMPLATE
     */
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself = 2;
}
