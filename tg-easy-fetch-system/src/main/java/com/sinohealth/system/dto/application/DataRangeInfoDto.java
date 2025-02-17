package com.sinohealth.system.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-12 14:45
 * @Desc
 * @see FilterDTO.FilterItemDTO
 */
@ApiModel(description = "数据范围信息")
@Data
public class DataRangeInfoDto {
    @ApiModelProperty("列ID")
    private Long tableId;
    @ApiModelProperty("表名称")
    private String tableName;
    @ApiModelProperty("列名称")
    private Long colName;
    @ApiModelProperty("计算方式")
    private Long computeWay;

    @ApiModelProperty("连接条件")
    private String andOr;
    @ApiModelProperty("计算内容")
    private String content = "";

    @ApiModelProperty("类型")
    private String type;

    /**
     * @see CommonConstants
     */
    @ApiModelProperty("条件表达式")
    private Long conditions;

    @ApiModelProperty("自用字段, 数据范围内容")
    @JsonIgnore
    private String dataRangeContent;
    @ApiModelProperty("是否全选, 2为全选")
    private Integer isAllSelected = 2;
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself;

    private String copyfieldName;
    private Long isNecessary;
    private Integer length;

    @ApiModelProperty("条件组嵌套")
    private List<DataRangeInfoDto> dataRangeInfo;
}
