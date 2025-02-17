package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 【请填写功能名称】对象 table_filed_info
 *
 * @author dataplatform
 * @date 2021-04-24
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_field_info")
@ApiModel("TableFieldInfo")
public class TableFieldInfo extends Model<TableFieldInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String fieldName;

    private String fieldAlias;

    /**
     * 注意update时需要回填值
     *
     * @see FieldDict#id 字段库id
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long relationColId;

    /**
     * 注意 流转过程中都是使用不带精度 Nullable等修饰的原始类型
     */
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

    private Long tableId;

    @TableField(exist = false)
    private String tableName;

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

    @ApiModelProperty("是否为默认显示字段")
    private String defaultShow;

    @ApiModelProperty("逻辑主键")
    private Boolean logicKey;

    @ApiModelProperty("是否参与比对")
    private Boolean compareField;

}
