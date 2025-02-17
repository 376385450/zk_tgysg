package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.FieldUseWayEnum;
import com.sinohealth.system.biz.dict.service.UniqueDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 字段库 字典
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-05 13:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_field_dict")
@Accessors(chain = true)

public class FieldDict implements UniqueDomain<FieldDict> {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("字段英文名")
    private String fieldName;

    @ApiModelProperty("中文名")
    private String name;

    @ApiModelProperty("字段描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sort;

    /**
     * @see DataDictDataTypeEnum
     */
    private String dataType;

    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    /**
     * @see BizDataDictDefine#id
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long dictId;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    /**
     * @see FieldUseWayEnum
     */
    private String useWay;

    private Boolean enable;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @Override
    public String getBizName() {
        return String.format("%s-%s", fieldName, name);
    }

    @Override
    public void appendQuery(LambdaQueryWrapper<FieldDict> wrapper) {
        wrapper.or(v -> v.eq(FieldDict::getName, this.getName()).eq(FieldDict::getFieldName, this.getFieldName()));
    }
}
