package com.sinohealth.system.biz.rangepreset.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.system.biz.rangepreset.domain.base.CommonPreset;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 数据范围预设
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-07-20 14:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_range_preset")
@Accessors(chain = true)
public class RangePreset implements CommonPreset {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("预设范围名称")
    private String name;

    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("关联模板id")
    private Long templateId;

    /**
     * @see FilterDTO
     */
    @ApiModelProperty("范围")
    private String filters;

    /**
     * @see FieldGranularityEnum
     */
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    /**
     * @see TemplateTypeEnum
     */
    @ApiModelProperty("模板类型")
    private String templateType;

    /**
     * @see RangePresetTypeEnum
     */
    private String rangeType;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
