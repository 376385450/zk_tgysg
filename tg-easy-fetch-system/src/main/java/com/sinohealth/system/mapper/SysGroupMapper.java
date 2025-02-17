package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.system.dto.UserGroupDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户分组Mapper接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
public interface SysGroupMapper extends BaseMapper<SysGroup> {

    @Select({"<script>", "select g.id as groupId,g.group_name,u.user_name,g.description,g.status,u.user_name as leader from sys_group g left join sys_user u on u.user_id=g.group_leader_id ",
            " where g.status =1  <if test=\"name!=null and name!='' \">", " and g.group_name like CONCAT('%',#{name},'%')", "</if>",
            " <if test=\"userId!=null  \"> and g.id in ( select u.group_id from sys_user_group u where u.user_id=#{userId} )", "</if>", "</script>"})
    public List<UserGroupDto> queryList(@Param("name") String name, @Param("userId") Long userId);

    @Select({"<script>", "select g.id as groupId,g.group_name,u.user_name,g.description,g.status,u.user_name as leader from sys_group g left join sys_user u on u.user_id=g.group_leader_id ",
            " where g.status =1  <if test=\"name!=null and name!='' \">", " and g.group_name like CONCAT('%',#{name},'%')", "</if>",
            " <if test=\"userId!=null  \">  and g.group_leader_id=#{userId} ", "</if>", "</script>"})
    public List<UserGroupDto> queryListAdmin(@Param("name") String name, @Param("userId") Long userId);

    @Select(" select id from sys_group where group_leader_id=#{leaderId}")
    public List<Long> getGroupIdByLeaderId(@Param("leaderId") Long leaderId);
}
