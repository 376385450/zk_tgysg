package com.sinohealth.system.biz.dict.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 16:04
 */
@Data
public class MetricsFormulaVO {

    @ApiModelProperty("指标")
    private List<Long> metrics;

    public MetricsFormulaVO() {
    }

    public MetricsFormulaVO(List<Long> metrics) {
        this.metrics = metrics;
    }
}
