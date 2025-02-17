package com.sinohealth.system.biz.rangepreset.dto;

import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-28 16:13
 */
@Data
public class RangeTemplatePresetDTO {

    private Long id;

    @ApiModelProperty("预设范围名称")
    private String name;

    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("模板id")
    private Long templateId;

    /**
     * 自定义列 List<TgDataRangeGroupVO> 结构
     *
     * @see TgDataRangeGroupVO
     */
    @ApiModelProperty(name = "分组信息")
    private String groupList;

    /**
     * @see FieldGranularityEnum
     */
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    @ApiModelProperty("所属模板")
    private String template;

    /**
     * @see RangePresetTypeEnum
     */
    private String rangeType;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
