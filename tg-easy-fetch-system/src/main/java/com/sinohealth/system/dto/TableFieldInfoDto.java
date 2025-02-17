package com.sinohealth.system.dto;

import com.sinohealth.system.biz.dict.domain.FieldDict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 【请填写功能名称】对象 table_filed_info
 *
 * @author dataplatform
 * @date 2021-04-24
 */
@Data
@ApiModel("TableFieldInfoDto")
public class TableFieldInfoDto {

    private Long id;
    @ApiModelProperty("字段英文名")
    @NotNull
    private String fieldName;
    @ApiModelProperty("字段中文名")
    private String fieldAlias;

    @ApiModelProperty("数据类型")
    @NotNull
    private String dataType;
    @NotNull
    private Boolean primaryKey;
    @NotNull
    private Boolean empty;

    private String comment;

    private Long tableId;

    private Long dirId;

    private boolean status;

    @ApiModelProperty("字段长度")
    private int length;
    @ApiModelProperty("小数位长度")
    private int scale;

    @ApiModelProperty("字段排序")
    private Integer sort;

    @ApiModelProperty("重点字段，true是，false否")
    private boolean majorField = false;

    /**
     * @see FieldDict#id
     */
    private Long relationColId;

    @ApiModelProperty("字段类型")
    private String fieldType;

    @ApiModelProperty("维度/指标")
    private String dimIndex;

    @ApiModelProperty("对外名称")
    private String realName;

    @ApiModelProperty("是否为默认显示字段")
    private String defaultShow;

    @ApiModelProperty("逻辑主键")
    private Boolean logicKey;

    @ApiModelProperty("是否参与比对")
    private Boolean compareField;
}
