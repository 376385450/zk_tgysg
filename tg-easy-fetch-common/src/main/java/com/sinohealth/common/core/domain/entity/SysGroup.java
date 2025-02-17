package com.sinohealth.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户分组对象 sys_group
 * 
 * @author jingjun
 * @date 2021-04-16
 */
@Getter
@Setter
@TableName("sys_group")
public class SysGroup
{
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String description;

    private Long groupLeaderId;

    private Long createUserId;

    private Date createTime;

    private Long updateUserId;

    private Date updateTime;

    private Integer status;


}
