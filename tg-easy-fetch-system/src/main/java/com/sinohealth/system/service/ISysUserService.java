package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.entity.SysMenu;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.HomeUserInfoDto;
import com.sinohealth.system.dto.UserCreateDto;
import com.sinohealth.system.dto.UserQuitDto;

import java.util.*;

/**
 * 用户 业务层
 *
 * @author dataplatform
 */
public interface ISysUserService extends IService<SysUser> {


    List<SysUserTable> getUserTableFromCache(Long userId, boolean refresh);

    List<Long> getUserDirIdsFromCache(Long userId, boolean refresh);

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    List<SysUser> selectUserList(SysUser user);

    List<SysUser> selectList(String userName, Integer roleid, Integer status, Integer pageNum, Integer pageSize);

    void insertUserTable(Long userId, DataDirDto tree, Integer defaultAccessType);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

    List<SysUser> selectUserByUserNames(Collection<String> userNames);

    Map<String, Long> selectUserByRealNames(Collection<String> realNames);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(Long userId);

    List<SysUser> getUnMappingUsers();

    List<SysUser> selectUserByIds(Collection<Long> userIds);

    Map<Long, SysUser> selectUserMapByIds(Collection<Long> userIds);

    /**
     * @return id -> realName
     */
    Map<Long, String> selectUserNameMapByIds(Collection<Long> userIds);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserRoleGroup(String userName);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    String checkUserNameUnique(String userName);

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    String checkPhoneUnique(SysUser user);

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    String checkEmailUnique(SysUser user);


    UserCreateDto getUserDetail(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int insertUser(SysUser user, List<Long> roles, List<Long> groupIds, DataDirDto tree, List<Long> menus);

    /**
     * 修改用户信息
     */
    int updateUser(UserCreateDto dto);

    /**
     * 从天宫过来修改用户
     * 不修改角色和菜单
     *
     * @param dto
     * @return
     */
    int updateFromTg(UserCreateDto dto);

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserStatus(SysUser user);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserProfile(SysUser user);

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    boolean updateUserAvatar(String userName, String avatar);

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    int resetPwd(SysUser user);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(String userName, String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserById(Long userId);

    List<SysUser> allSyncUser();

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    int deleteUserByIds(Long[] userIds);

    SysUser findByOrgUserId(String orgUserId);

    List<SysUser> getGroupMember(Long groupId);

    void updateLoginTime(Long userId, Date now);

    HomeUserInfoDto getUserInfo();

    Set<Long> getGroupLeaderIdByUserId(Collection<Long> userIds);

    Set<Long> getAdmin();

    void updateLoginTimeToken(String token, Long userId, Date now);

    SysUser getUserByToken(String token);

    int insertSubAccount(SysUser user);

    SysUser selectUserByPhoneOrEmail(String keyword);

    int resetPwdByPhoneOrEmail(String keyword, String password);

    int selectCntByPhone(String phoneNum);

    int bindPhoneNum(String username, String phoneNum);

    int checkOrgIdUnique(String orgId);

    void handleQuitUser();

    List<UserQuitDto> selectQuitUsers(String realName);

    void batchUpdateQuit(List<UserQuitDto> userQuitDtos);

    String getUserViewName(Long... userIds);

    String getUserOnlyName(Long... userIds);

    String getUserViewName(Collection<SysUser> users);

    Long getUserIdByRealName(String realName);

    Map<String, Long> getUserIdByRealNames(Collection<String> realName);

    List<SysMenu> queryFastEntryInfo(Map<String, Object> parameterMap);
}
