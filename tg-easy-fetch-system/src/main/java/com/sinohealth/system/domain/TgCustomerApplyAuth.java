package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tg_customer_apply_auth")
public class TgCustomerApplyAuth {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("数据资产id")
    private Long assetsId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("报表权限：1:查看;2下载")
    private String authType;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * @see com.sinohealth.common.enums.StatusTypeEnum
     */
    private Integer status;

    private String outTableName;

    private String nodeName;

    private String icon;

    private Long parentId;

    private Long nodeId;

    /**
     * 如果是子账号的授权资产，这个字段表示对应的父账号的授权资产id
     */
    private Long parentCustomerAuthId;

    @TableField(exist = false)
    private String customerType;

}
