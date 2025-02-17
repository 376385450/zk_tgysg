package com.sinohealth.system.biz.application.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-14 14:55
 */
@Data
public class ApplyMetricsDto {

    private Long id;

    @ApiModelProperty("指标英文名")
    private String fieldName;

    @ApiModelProperty("中文名")
    private String name;

    @ApiModelProperty("小数精度")
    private Integer precisionNum;
}
