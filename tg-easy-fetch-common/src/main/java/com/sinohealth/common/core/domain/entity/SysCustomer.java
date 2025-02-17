package com.sinohealth.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("sys_customer")
public class SysCustomer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String   fullName;
    private String   abbreviationName;
     private Integer isCount;
    private  Integer manageUser;
    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 状态：1正常，2停用
     */
    private Integer status;

    //主数据id
    private String serviceId;

    private  String manageUserOri;

    private String customerType;

}
