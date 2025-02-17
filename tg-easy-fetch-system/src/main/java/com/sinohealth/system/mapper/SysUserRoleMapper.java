package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author dataplatform
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select({"<script>"," select r.role_name,u.user_id,u.role_id from sys_user_role u , sys_role r  where u.user_id in ","<foreach  item='item' index='index' collection='userIds' open='(' separator=',' close=')'> #{item}  </foreach> "," and u.role_id=r.role_id </script>"})
    public List<SysUserRole> getUserRoleName(@Param("userIds")List<Long> userIds);
}
