package com.sinohealth.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.common.enums.dict.DivisorModeEnum;
import com.sinohealth.common.enums.dict.MetricsTypeEnum;
import com.sinohealth.system.biz.dict.dto.MetricsFormulaVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-30 16:19
 */
@Data
public class Val {

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
    @ApiModelProperty("指标类型")
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
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    @Override
    public String toString() {
        return
                "\"" + id + "\"" +
                        ", \"" + fieldName + "\"" +
                        ", \"" + name + "\"" +
                        ", \"" + meaning + "\"" +
                        ", \"" + metricsType + "\"" +
                        ", \"" + formula + "\"" +
                        ", \"" + formulaDisplay + "\"" +
                        ", \"" + originFieldAlias + "\"" +
                        ", \"" + originFieldName + "\"" +
                        ", \"" + colName + "\"" +
                        ", \"" + computeWay + "\"" +
                        ", \"" + formulaDef + "\"" +
                        ", \"" + depMetrics + "\"" +
                        ", \"" + precisionNum + "\"" +
                        ", \"" + divisorMode + "\"" +
                        ", \"" + remark + "\"" +
                        ", \"" + bizType.replace(",", " ") + "\"" +
                        ", \"" + creator + "\"" +
                        ", \"" + updater + "\"" +
                        ", \"" + createTime + "\"" +
                        ", \"" + updateTime + "\""
        ;
    }
}
