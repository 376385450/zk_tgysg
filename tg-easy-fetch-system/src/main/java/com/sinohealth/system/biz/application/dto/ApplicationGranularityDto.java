package com.sinohealth.system.biz.application.dto;

import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-22 14:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationGranularityDto {

    /**
     * @see FieldGranularityEnum
     */
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    /**
     * @see TemplateGranularityDetailDto#name
     */
    @ApiModelProperty("选择的粒度")
    private List<String> selectGranularity;

    @ApiModelProperty("选择的字段")
    private List<SelectFieldDto> fields;

    @ApiModelProperty("数据范围")
    private FilterDTO filter;

    /**
     * @see com.sinohealth.system.domain.TgDataRangeTemplate#id
     */
    @ApiModelProperty("自定义范围id")
    private Long rangeTemplateId;
}
