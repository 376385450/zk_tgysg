package com.sinohealth.system.biz.rangepreset.dto.request;

import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 15:02
 */
@Data
public class RangePresetUpsertRequest {

    private Long id;

    @ApiModelProperty("预设范围名称")
    @NotBlank(message = "名称为空")
    private String name;

    @ApiModelProperty("业务线")
    @NotBlank(message = "业务线必填")
    private String bizType;

    @ApiModelProperty("模板id")
    @NotNull(message = "模板必选")
    private Long templateId;

    /**
     * @see FilterDTO
     */
    @ApiModelProperty("范围")
    private String filters;

    /**
     * @see FieldGranularityEnum
     */
    @ApiModelProperty("分类 粒度")
    @NotBlank(message = "分类必填")
    private String granularity;

    /**
     * @see RangePresetTypeEnum
     */
    @NotBlank(message = "预设类型为空")
    private String rangeType;

}
