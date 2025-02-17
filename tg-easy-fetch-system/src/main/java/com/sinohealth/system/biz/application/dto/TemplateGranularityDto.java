package com.sinohealth.system.biz.application.dto;

import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-22 14:16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGranularityDto {

    /**
     * 粒度选择是否多选
     */
    @ApiModelProperty("是否多选")
    private Boolean multiple;

    @ApiModelProperty("是否开启自定义列")
    private Boolean enableRangeTemplate;
    @ApiModelProperty("自定义列 提示")
    private String rangeTemplateTips;

    @ApiModelProperty("自定义列是否必填")
    private Boolean rangeTemplateRequired;

    @ApiModelProperty("是否开启数据范围筛选")
    private Boolean enableFilter;

    @ApiModelProperty("数据筛选设置-提示")
    private String filterTips;

    @ApiModelProperty("数据范围筛选是否必填")
    private Boolean filterRequired;

    @ApiModelProperty("粒度是否必填")
    private Boolean granularityRequired;

    @ApiModelProperty("粒度说明")
    private String granularityTips;

    /**
     * @see FieldGranularityEnum
     */
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    @ApiModelProperty("分类提示")
    private String tips;

    @ApiModelProperty("列说明")
    private String columnTips;

    @ApiModelProperty("前端用 所有粒度分区的 必选字段")
    private List<Long> required;

    @ApiModelProperty("粒度值 列表")
    private List<TemplateGranularityDetailDto> details;
}
