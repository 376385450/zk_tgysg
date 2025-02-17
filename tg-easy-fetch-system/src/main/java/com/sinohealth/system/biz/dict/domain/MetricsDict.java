package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DivisorModeEnum;
import com.sinohealth.common.enums.dict.MetricsTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.system.biz.dict.service.UniqueDomain;
import com.sinohealth.system.util.ApplicationSqlUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 指标字典
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-05 13:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_metrics_dict")
@Accessors(chain = true)
public class MetricsDict implements UniqueDomain<MetricsDict> {

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

    private String formulaDisplay;

    @ApiModelProperty("字段库 别名")
    private String originFieldAlias;
    @ApiModelProperty("字段库 字段名")
    private String originFieldName;
    @ApiModelProperty("字段库 字段id")
    private Long colName;
    @ApiModelProperty("计算方式")
    private Integer computeWay;

    /**
     * 依赖的指标id 逗号分隔
     */
    private String depMetrics;

    /**
     * 用于 计算指标，预设指标 精度默认0
     */
    @ApiModelProperty("小数精度")
    private Integer precisionNum;

    /**
     * @see DivisorModeEnum
     */
    @ApiModelProperty("除数为0的处理模式")
    private String divisorMode;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("排序")
    private Integer sort;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 外层指标二次计算
     * <p>
     * 模板层 SUM(t_1.fd_xsl)  `放大销售量`
     * 申请层 SUM(`放大销售量`) `放大销售量`
     */
    public String buildApplyMetricsFormula() {
        Optional<CommonConstants.ComputeWayEnum> enumOpt = CommonConstants.ComputeWayEnum.getById(computeWay);
        String field = "`" + name + ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX + "`";
        return enumOpt
                .map(e -> e.buildApplyPrecisionExpression(field, precisionNum))
                .orElse(null);
    }

    /**
     * 申请层 预设指标 参数计算的 指标表达式 不能做精度计算
     */
    public String buildApplyPresetMetricsFormula() {
        Optional<CommonConstants.ComputeWayEnum> enumOpt = CommonConstants.ComputeWayEnum.getById(computeWay);
        String field = "`" + name + ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX + "`";
        if (!enumOpt.isPresent()) {
            throw new CustomException("不支持的指标计算方式");
        }
        return enumOpt.get().buildApplyPrecisionExpression(field, null);
    }

    /**
     * 单层 模板 聚合出外层指标
     *
     * @param fieldName 真实字段名
     */
    public String buildSingleTemplateMetricsFormula(String fieldName) {
        Optional<CommonConstants.ComputeWayEnum> enumOpt = CommonConstants.ComputeWayEnum.getById(computeWay);
        return enumOpt.map(e -> e.buildApplyPrecisionExpression(fieldName, precisionNum)).orElse(null);
    }

    @Override
    public String getBizName() {
        return String.format("%s-%s", fieldName, name);
    }

    @Override
    public void appendQuery(LambdaQueryWrapper<MetricsDict> wrapper) {
        wrapper.or(v -> v.eq(MetricsDict::getName, this.getName()).eq(MetricsDict::getFieldName, this.getFieldName()));
    }
}
