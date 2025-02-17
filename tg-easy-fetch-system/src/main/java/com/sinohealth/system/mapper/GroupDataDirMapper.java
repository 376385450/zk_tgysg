package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.GroupDataDir;
import com.sinohealth.system.dto.GroupLeaderDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
public interface GroupDataDirMapper extends BaseMapper<GroupDataDir> {
    @Select({"<script>", " select dir_id  from group_data_dir where group_id in ",
            " <foreach collection='groupIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> ", "</script>"})
    public List<Long> getDirId(@Param("groupIds") List<Long> groupIds);


    @Select(" select d.dir_id from group_data_dir d , sys_group g where d.group_id=g.id and g.group_leader_id = #{userId}")
    public List<Long> getDirIdByLeaderId(@Param("userId") Long userId);

    @Select(" select distinct d.dir_id from group_data_dir d where d.group_id in ( select u.group_id from sys_user_group u where u.user_id=#{userId})")
    public List<Long> getDirIdByUserId(@Param("userId") Long userId);
    @Select(" select g.group_name,u.user_name from sys_group g LEFT JOIN sys_user u on u.user_id= g.group_leader_id  where g.id in (select group_id from group_data_dir where dir_id=#{dirId} )")
    public List<GroupLeaderDto> queryGroupLeader(@Param("dirId")Long dirId);

    @Select("select t3.group_name, t2.user_name from sys_user_group t left join sys_user t2 on t2.user_id = t.user_id left join sys_group t3 on t3.id = t.group_id where t.group_id in ( select group_id from group_data_dir where dir_id = #{dirId} )")
    List<GroupLeaderDto> queryGroupUser(@Param("dirId") Long dirId);
 }
