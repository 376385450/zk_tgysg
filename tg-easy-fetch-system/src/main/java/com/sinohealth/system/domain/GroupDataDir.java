package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 【请填写功能名称】对象 group_data_dir
 * 
 * @author jingjun
 * @date 2021-04-16
 */
@Data
@TableName("group_data_dir")
public class GroupDataDir
{
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long dirId;



}
