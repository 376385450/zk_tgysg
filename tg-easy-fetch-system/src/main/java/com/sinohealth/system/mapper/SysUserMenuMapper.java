package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.SysUserMenu;
import com.sinohealth.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户与菜单关联表 数据层
 *
 * @author dataplatform
 */
@Mapper
public interface SysUserMenuMapper extends BaseMapper<SysUserMenu> {


}
