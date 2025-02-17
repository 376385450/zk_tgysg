package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("保存BI图表参数")
@Data
public class SaveArkbiParam {
    @ApiModelProperty("BI分析ID")
    private String extAnalysisId;
}
