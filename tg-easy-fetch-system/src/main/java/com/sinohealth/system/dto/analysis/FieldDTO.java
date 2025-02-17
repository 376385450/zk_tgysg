package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 字段信息
 *
 * @author linkaiwei
 * @date 2021/08/18 17:15
 * @since 1.4.1.0
 */
@ApiModel("字段信息")
@Data
@Accessors(chain = true)
public class FieldDTO implements Serializable {

    @ApiModelProperty("字段ID")
    private Long fieldId;

    @ApiModelProperty("英文名称")
    private String fieldName;

    @ApiModelProperty("中文名称")
    private String fieldAlias;

    /**
     * 详情见 {@link com.sinohealth.bi.enums.SqlTypeEnum}
     *
     * @date 2021-08-19 14:33:46
     * @since 1.4.1.0
     */
    @ApiModelProperty("数据库表字段类型")
    private String sqlType;

    /**
     * 详情见 {@link com.sinohealth.bi.enums.VisualTypeEnum}
     *
     * @date 2021-08-19 14:33:46
     * @since 1.4.1.0
     */
    @ApiModelProperty("可视化类型，number数字，string字符串，date日期")
    private String visualType;

    /**
     * 详情见 {@link com.sinohealth.bi.enums.ModelTypeEnum}
     *
     * @date 2021-08-19 14:33:46
     * @since 1.4.1.0
     */
    @ApiModelProperty("数据类型，dimension维度，measure度量")
    private String modelType;

    @ApiModelProperty("唯一id")
    private Long uniqueId;

    @ApiModelProperty("表ID")
    private Long tableId;

    /**
     * 详情见 {@link com.sinohealth.bi.enums.ExpressionTypeEnum}
     *
     * @date 2021-08-19 14:33:46
     * @since 1.4.1.0
     */
    @ApiModelProperty("字段表达式类型，origin源字段，calculate四则运算，splice字符拼接，customize自定义计算")
    private String expressionType;

    @ApiModelProperty("计算表达式")
    private String expression;

    @ApiModelProperty("小数位长度，0保留整数，1保留1位小数，2保留2位小数")
    private Integer scale;

    // 以下字段是前端用
    private String field;
    private String fields;
    private String splicingOperator;
    private String splicingOperatorValue;
    private String searchValue;
    private String fieldCalculation;
    private String tableIds;

}
