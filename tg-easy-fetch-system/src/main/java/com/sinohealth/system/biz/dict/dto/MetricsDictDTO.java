package com.sinohealth.system.biz.dict.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.common.enums.dict.DivisorModeEnum;
import com.sinohealth.common.enums.dict.MetricsTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 15:40
 */
@Data
@ApiModel(description = "指标")
public class MetricsDictDTO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("指标英文名")
    private String fieldName;

    @ApiModelProperty("中文名")
    private String name;

    @ApiModelProperty("指标含义")
    private String meaning;

    /**
     * @see MetricsTypeEnum
     */
    private String metricsType;

    @ApiModelProperty("计算公式/逻辑")
    private String formula;

    /**
     * 前端展示公式
     */
    private String formulaDisplay;

    @ApiModelProperty("字段库 别名")
    private String originFieldAlias;
    @ApiModelProperty("字段库 字段名")
    private String originFieldName;
    @ApiModelProperty("字段库 字段id")
    private Long colName;
    @ApiModelProperty("计算方式")
    private Integer computeWay;

    private MetricsFormulaVO formulaDef;

    /**
     * 依赖的指标id 逗号分隔
     */
    private String depMetrics;

    @ApiModelProperty("小数精度")
    private Integer precisionNum;

    /**
     * @see DivisorModeEnum
     */
    @ApiModelProperty("除数为0的处理模式")
    private String divisorMode;

    @ApiModelProperty("备注")
    private String remark;

    private String bizType;

    @ApiModelProperty("创建人名称")
    private String creator;

    @ApiModelProperty("更新人名称")
    private String updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
