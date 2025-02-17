package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 【用于存储模板或申请的临时字段列】对象 custom_field_info
 *
 * @author dataplatform
 * @date 2021-04-24
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("custom_field_info")
@ApiModel("CustomFieldInfo")
public class CustomFieldInfo extends Model<CustomFieldInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String fieldName;

    private String fieldAlias;

    @ApiModelProperty("计算方式")
    private Integer computeWay;

    private String dataType;

    @ApiModelProperty("字段长度")
    private int length;
    @ApiModelProperty("小数位长度")
    private int scale;

    private boolean primaryKey;

    @TableField("`empty`")
    private boolean empty;

    @TableField("`comment`")
    private String comment;

    private Long sourceId;

    private Long tableId;

    private Long dirId;

    private Date createTime;

    private Long createUserId;

    private Date updateTime;

    private Long updateUserId;

    private Boolean status = false;

    /**
     * 字段排序
     */
    private Integer sort = 0;

    @TableField(exist = false)
    private String mappingName = "无";

    /**
     * 重点字段，true是，false否
     */
    private Boolean majorField = false;

    private String fieldType;

    private String dimIndex;

    @ApiModelProperty("对外名称")
    private String realName;

    private Integer source;

    @ApiModelProperty("是否隐藏(模板中被删除的指标)")
    private Boolean hiddenForApply;

    /**
     * 字段来源
     */
    @TableField(exist = false)
    private Integer fieldSource;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
