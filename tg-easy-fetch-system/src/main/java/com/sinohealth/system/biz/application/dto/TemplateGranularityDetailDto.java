package com.sinohealth.system.biz.application.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-22 14:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGranularityDetailDto {

    @ApiModelProperty("粒度值")
    private String name;

    @ApiModelProperty("粒度字段 必选")
    private List<Long> required;

    @ApiModelProperty("标签字段 非必选")
    private List<Long> options;

    @ApiModelProperty("自定义列粒度选择")
    private Boolean canChoose;

    /**
     * 是否 粒度默认勾选
     */
    private Boolean defaultCheck;

    @ApiModelProperty("是否换行")
    private Boolean newLine;

    @ApiModelProperty("粒度说明")
    private String tips;

    @ApiModelProperty("排序")
    private Integer sort;
}
