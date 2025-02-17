package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 表字段映射信息
 *
 * @author linkaiwei
 * @date 2021/11/04 10:13
 * @since 1.6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"id", "createTime", "createUserId"})
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_mapping")
public class TableMapping implements Serializable {

    @TableId(value = "id")
    private Long id;

    /**
     * 源表ID
     */
    private Long tableId;

    /**
     * 源字段ID
     */
    private Long fieldId;

    /**
     * 关联表数据目录ID
     */
    private Long relationDirId;

    /**
     * 关联表ID
     */
    private Long relationTableId;

    /**
     * 关联表字段ID
     */
    private Long relationFieldId;

    /**
     * 映射字段ID
     */
    private Long mappingFieldId;


    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人ID
     */
    private Long updateUserId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 状态：0删除，1正常，2停用
     */
    private Integer status;

}
