package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.system.biz.application.dto.PackTailFieldDto;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 模板打包配置关联表(TgTemplatePackTailSetting)表实体类
 *
 * @author zengjun
 * @since 2024-12-09 16:21:17
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "模板打包配置关联表(TgTemplatePackTailSetting)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_template_pack_tail_setting")
@Accessors(chain = true)
public class TgTemplatePackTailSetting extends Model<TgTemplatePackTailSetting> implements IdTable {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模板id")
    private Long templateId;

    @ApiModelProperty("打包名称")
    private String name;

    @ApiModelProperty("打包描述")
    private String description;

    @ApiModelProperty("打包长尾条件")
    @TableField(exist = false)
    private FilterDTO tailFilter;
    @JsonIgnore
    private String tailFilterJson;

    @TableField(exist = false)
    @ApiModelProperty("长尾处理字段")
    private List<PackTailFieldDto> tailFields;
    @JsonIgnore
    private String tailFieldsJson;

    @TableLogic
    @ApiModelProperty("是否删除")
    private Long deleted;
}

