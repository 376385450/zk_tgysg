package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.core.domain.entity.SysMenu;
import com.sinohealth.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户表 数据层
 *
 * @author dataplatform
 */
@Mapper
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据条件分页查询用户列表
     *
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUser selectUserByUserName(String userName);

    List<SysUser> selectUserByUserNames(@Param("userNames") Collection<String> userNames);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(Long userId);
    List<SysUser> selectUserByIds(@Param("userIds") Collection<Long> userIds);

    @Update(" update sys_user set login_date=#{loginDate},login_times=login_times+1 where user_id=#{userId}")
    public void updateLoginTime(@Param("userId") Long userId, @Param("loginDate") String loginDate);

    @Select("select user_id,user_name,real_name from sys_user u where u.user_id in ( select g.user_id from sys_user_group g where g.group_id =#{groupId} )")
    public List<SysUser> getGroupMember(@Param("groupId") Long groupId);

    @Select({"<script>",
            " select sg.group_leader_id from sys_group sg where sg.id in(" ,
            " select sug.group_id from sys_user su LEFT JOIN sys_user_group sug ON  su.user_id = sug.user_id where sug.group_id is not null and su.user_id in" ,
            " <foreach collection='userIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> ",
            " GROUP BY sug.group_id )",
            "</script>",
    })
    Set<Long> getGroupLeaderIdByUserId(@Param("userIds")Collection<Long> userIds);

    @Select("select DISTINCT sur.user_id from sys_role sr LEFT JOIN sys_user_role sur on sr.role_id = sur.role_id where sr.role_id in(1,2) or sr.role_name in('超级管理员','管理员')")
    Set<Long> getAdmin();

    @Update(" update sys_user set login_date=#{loginDate},login_times=login_times+1,token=#{token} where user_id=#{userId} ")
    public void updateLoginTimeToken(@Param("userId")Long userId,  @Param("loginDate")String parseDateToStr, @Param("token")String token);

    /**
     * 查询相应组的用户
     * @param userName
     * @return
     */
    List<SysUser> selectLists(@Param("userName")String userName, @Param("roleId")Integer roleId,@Param("status")Integer status);

    /**
     * 根据用户真实姓名，查询用户列表
     *
     * @param realName 真实姓名
     * @return 用户列表
     * @author linkaiwei
     * @date 2021-08-16 14:50:44
     * @since 1.4.1.0
     */
    @Select("select * from sys_user where real_name like concat('%', #{realName}, '%')")
    List<SysUser> listSysUserByRealName(@Param("realName") String realName);

    SysUser selectUserByPhoneOrEmail(@Param("keyword") String keyword);

    int selectCntByPhone(String phoneNum);

    int selectCntByOrgId(String  orgId);

    List<SysUser> selectAllUserForQuit();

    List<SysUser> selectInternalStaff(String realName);

    List<Integer> selectUserAllCnt(@Param("username") String username,@Param("userId") Long userId);

    List<SysMenu> queryFastEntryInfo(@Param("parameter") Map<String, Object> parameterMap);

    Set<Long> findIdByOrgIds(@Param("orgUserIds")Set<String> orgUserIds);

    Map<String, String> queryOrgBriefName(@Param("pathName") String orgAdminTreePathText);
}
