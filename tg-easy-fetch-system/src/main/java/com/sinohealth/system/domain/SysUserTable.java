package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 【请填写功能名称】对象 sys_user_table
 * 
 * @author dataplatform
 * @date 2021-04-21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_user_table")
public class SysUserTable implements Serializable {

    private static final long serialVersionUID=1L;


    /**  */
    @TableId(value = "id")
    private Long id;

    /**  */
    private Long userId;

    /**  */
    private Long tableId;

    private Long dirId;

    /** 1-5 */
    private Integer accessType;

    private boolean concern;

}
