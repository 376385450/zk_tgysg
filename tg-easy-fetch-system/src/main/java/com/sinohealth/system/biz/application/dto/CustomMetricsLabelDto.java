package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.biz.dict.domain.MetricsDict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 指标列设置 （模板和申请）
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-08 14:32
 */
@Data
public class CustomMetricsLabelDto {

    /**
     * 共用 指标id
     * @see MetricsDict#id
     */
    private Long metricsId;

    @ApiModelProperty("共用 申请：是否选中 模板：是否初始勾选")
    private Boolean select;

    /**
     * @see com.sinohealth.common.constant.CommonConstants#GT
     */
    @ApiModelProperty("模板使用 计算方式")
    private Integer conditions;

    @ApiModelProperty("模板使用 计算内容")
    private String content;

    /**
     * 申请使用 指标别名
     */
    private String alias;

}
