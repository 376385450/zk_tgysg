package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.system.domain.SysUserGroup;
import com.sinohealth.system.dto.UserGroupDto;
import com.sinohealth.system.dto.common.IdAndName;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 用户组关联Mapper接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
public interface SysUserGroupMapper extends BaseMapper<SysUserGroup> {

    @Select({"<script> select t.group_id,count(t.group_id) countMember from sys_user_group t where t.group_id in ",
            "<foreach  item='item' index='index' collection='ids' open='(' separator=',' close=')'> #{item}  </foreach> ", " group by group_id </script>"})
    public List<UserGroupDto> countUserGroup(@Param("ids") List<Long> ids);

    @Select(" select * from sys_user_group t where t.group_id =#{groupId}")
    public List<SysUserGroup> getByGroupId(@Param("groupId") Long groupId);

    @Select({"<script>",
            " SELECT sug.user_id as id,sg.group_name as name FROM sys_user_group sug LEFT JOIN sys_group sg on sug.group_id = sg.id where sug.user_id in",
            " <foreach  item='item' index='index' collection='userIds' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</script>"})
    List<IdAndName> selectGroupNameByUserId(@Param("userIds") Collection<Long> userIds);
}
