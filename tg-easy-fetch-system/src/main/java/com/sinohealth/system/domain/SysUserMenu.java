package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author dataplatform
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_user_menu")
public class SysUserMenu {

    private Long userId;

    private Long menuId;

}
