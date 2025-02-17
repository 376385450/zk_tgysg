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
 * 【请填写功能名称】对象 table_relation
 * 
 * @author dataplatform
 * @date 2021-04-27
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"id","createTime","createUserId"})
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_relation")
public class TableRelation implements Serializable {

private static final long serialVersionUID=1L;


    
    @TableId(value = "id")
    private Long id;

    private Long fieldId;

    private Long tableId;

    private Long dirId;

    
    private Long refFieldId;

    
    private Long refTableId;

    
    private Long createUserId;

    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


}
