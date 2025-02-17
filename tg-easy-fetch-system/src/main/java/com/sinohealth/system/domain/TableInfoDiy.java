package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@TableName("table_info_diy")
public class TableInfoDiy implements Serializable {

    @TableId(value = "id")
    private Long id;

    /**
     * 表ID
     */
    private Long tableId;

    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人ID
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 状态：0删除，1正常，2停用
     */
    private Integer status;

    @ApiModelProperty("表单信息")
    @TableField(exist = false)
    private TableInfo tableInfo;
}
