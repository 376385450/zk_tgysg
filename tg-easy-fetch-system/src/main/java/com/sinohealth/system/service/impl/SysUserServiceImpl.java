package com.sinohealth.system.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.pagehelper.PageHelper;
import com.sinohealth.common.annotation.DataScope;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.UserConstants;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysMenu;
import com.sinohealth.common.core.domain.entity.SysRole;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.HomeUserInfoDto;
import com.sinohealth.system.dto.UserCreateDto;
import com.sinohealth.system.dto.UserQuitDto;
import com.sinohealth.system.dto.common.IdAndName;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author dataplatform
 */
@Slf4j
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private ISysGroupService groupService;
    @Autowired
    private ISysUserGroupService userGroupService;
    @Autowired
    private ISysUserTableService userTableService;

    @Autowired
    private IGroupDataDirService groupDataDirService;

    @Autowired
    private IDataDirService dataDirService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysGroupServiceImpl sysGroupService;

    @Autowired
    private SysUserMenuMapper sysUserMenuMapper;

    @Autowired
    private ISysCustomerService sysCustomerService;

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;

    @Autowired
    private RelationTableManageService relationTableManageService;

    @Autowired
    private IAuditProcessService auditProcessService;

    @Autowired
    private SinoipaasUtils sinoipaasUtils;

    @Autowired
    private TgIntelligenceUserMappingMapper tgIntelligenceUserMappingMapper;

    static Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(12L))
            .build();


    public static String USER_TABLE_LIST = "USER_TABLE_LIST_";
    public static String USER_DIR_LIST = "USER_DIR_LIST_";
    @Resource(name = "redisTemplate")
    private ValueOperations<String, List<SysUserTable>> userTableCache;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, List<Long>> userDirCache;

    @Resource
    @Qualifier(ThreadPoolType.POST_MSG)
    private ThreadPoolTaskExecutor pool;

    @PostConstruct
    public void loadUser() {
        pool.submit(() -> {
            SinoipaasUtils.cache.invalidateAll();
            log.info("start load user");
            long start = System.currentTimeMillis();
            List<SysUser> sysUsers = baseMapper.selectList(new QueryWrapper<SysUser>().lambda()
                    .select(SysUser::getOrgUserId));
            Set<String> ids = sysUsers.stream().filter(Objects::nonNull).map(SysUser::getOrgUserId)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            for (String id : ids) {
                sinoipaasUtils.mainEmployeeSelectbyid(id);
            }
            log.info("finish load user {}. cost {}ms", ids.size(), System.currentTimeMillis() - start);
        });
    }

    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void reloadUser() {
        this.loadUser();
    }


    @Override
    public List<SysUserTable> getUserTableFromCache(Long userId, boolean refresh) {
        String key = USER_TABLE_LIST + userId;
        List<SysUserTable> list = refresh ? null : userTableCache.get(key);
        if (ObjectUtils.isEmpty(list)) {
            list = userTableService.list(Wrappers.<SysUserTable>query().eq("user_id", userId));
            if (list != null) {
                userTableCache.set(key, list, 1, TimeUnit.HOURS);
            }
        }

        return list;
    }

    @Override
    public List<Long> getUserDirIdsFromCache(Long userId, boolean refresh) {
        String key = USER_DIR_LIST + userId;

        List<Long> list = refresh ? null : userDirCache.get(key);
        if (ObjectUtils.isEmpty(list)) {
            list = groupDataDirService.getDirIdByUserId(userId);
            if (list != null) {
                userDirCache.set(key, list, 1, TimeUnit.HOURS);
            }
        }

        return list;

    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user) {
        return baseMapper.selectUserList(user);
    }

    @Override
    public List<SysUser> selectList(String userName, Integer roleid, Integer status, Integer pageNum, Integer pageSize) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        //判断用户组权限
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = baseMapper.selectLists(userName, roleid, status);

        // 应使用批量查询
//        Set<String> ids = list.stream().map(SysUser::getOrgUserId)
//                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
//        List<SinoPassUserDTO> allUser = SinoipaasUtils.mainEmployeeByIds(ids);

        for (SysUser sysUser : list) {
            if (StringUtils.isNotEmpty(sysUser.getOrgUserId())) {
                sysUser.setSinoPassUserDTO(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId()));
            }
            //处理创建人字段
            String key = "sys_user:create_by:" + sysUser.getCreateBy();
            Object viewName = cache.getIfPresent(key);
            if (viewName != null) {
                sysUser.setCreateByOri(String.valueOf(viewName));
                continue;
            }
            if (CommonConstants.SYSTEM_INIT.equals(sysUser.getCreateBy())) {
                continue;
            }

            SysUser createUser = this.selectUserByUserName(sysUser.getCreateBy());
            if (createUser != null && StringUtils.isNotEmpty(createUser.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(createUser.getOrgUserId());
                if (sinoPassUserDTO != null) {
                    viewName = sinoPassUserDTO.getViewName();
                    sysUser.setCreateByOri(String.valueOf(viewName));
                    Optional.ofNullable(viewName).ifPresent(value -> cache.put(key, value));
                }
            }
        }
        return list;
    }


    @Override
    public UserCreateDto getUserDetail(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new CustomException("找不到用户");
        }

        UserCreateDto dto = new UserCreateDto();
        BeanUtils.copyProperties(user, dto);
        dto.setPassword(null);
        dto.setLastTime(user.getLoginDate());
        dto.setLoginNum(user.getLoginTimes());
        List<SysUserTable> tableList = userTableService.list(Wrappers.<SysUserTable>query().eq("user_id", userId));
        List<SysUserGroup> groupList = userGroupService.list(Wrappers.<SysUserGroup>query().eq("user_id", userId));
        List<Long> groupIds = groupList.stream().map(SysUserGroup::getGroupId).collect(Collectors.toList());
        dto.setTree(dataDirService.getGroupTree(groupIds, true, tableList, false));
        dto.setRoleIds(roleService.selectRoleListByUserId(userId));
        dto.setGroupIds(groupIds);
        List<SysUserMenu> menuList = sysUserMenuMapper.selectList(Wrappers.<SysUserMenu>query().eq("user_id", userId));
        List<Long> menus = menuList.stream().map(SysUserMenu::getMenuId).collect(Collectors.toList());
        dto.setMenus(menus);
        if (dto.getUserInfoType() == 3) {
            dto.setSysCustomer(sysCustomerService.getByUserId(dto.getUserId()));
        }
        //处理用户组织机构
        if (StringUtils.isNotEmpty(user.getOrgUserId())) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
            if (sinoPassUserDTO != null) {
                dto.setSinoPassUserDTO(sinoPassUserDTO);
            }
        }


        return dto;

    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return baseMapper.selectUserByUserName(userName);
    }

    @Override
    public List<SysUser> selectUserByUserNames(Collection<String> userNames) {
        if (CollectionUtils.isEmpty(userNames)) {
            return Collections.emptyList();
        }
        return baseMapper.selectUserByUserNames(userNames);
    }

    @Override
    public Map<String, Long> selectUserByRealNames(Collection<String> realNames) {
        if (CollectionUtils.isEmpty(realNames)) {
            return Collections.emptyMap();
        }
        List<SysUser> users = baseMapper.selectList(new QueryWrapper<SysUser>().lambda()
                .select(SysUser::getRealName, SysUser::getUserId)
                .in(SysUser::getRealName, realNames));
        return Lambda.buildMap(users, SysUser::getRealName, SysUser::getUserId);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return baseMapper.selectUserById(userId);
    }

    @Override
    public List<SysUser> getUnMappingUsers() {
        final List<TgIntelligenceUserMapping> tgIntelligenceUserMappings = tgIntelligenceUserMappingMapper.selectList(Wrappers.emptyWrapper());

        final LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getStatus, 0)
                .eq(SysUser::getDelFlag, 0)
                .eq(SysUser::getUserInfoType, 2)
                .notIn(SysUser::getUserId, tgIntelligenceUserMappings.stream().map(TgIntelligenceUserMapping::getYsgUserId).collect(Collectors.toList()));

        final List<SysUser> users = baseMapper.selectList(sysUserLambdaQueryWrapper);

        return users;
    }

    @Override
    public List<SysUser> selectUserByIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectUserByIds(userIds);
    }

    @Override
    public Map<Long, SysUser> selectUserMapByIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        Map<Long, SysUser> nameMap;
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<SysUser> users = this.selectUserByIds(userIds);
            nameMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));
        } else {
            nameMap = Collections.emptyMap();
        }
        return nameMap;
    }

    @Override
    public Map<Long, String> selectUserNameMapByIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        Map<Long, String> nameMap;
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<SysUser> users = this.selectUserByIds(userIds);
            nameMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getRealName, (front, current) -> current));
        } else {
            nameMap = Collections.emptyMap();
        }
        return nameMap;
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleService.selectRolesByUserName(userName);
        StringBuilder idsStr = new StringBuilder();
        for (SysRole role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (Validator.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }


    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    @Override
    public String checkUserNameUnique(String userName) {
        int count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName).last("limit 1"));
        if (count > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkPhoneUnique(SysUser user) {
        Long userId = Validator.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = getOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getPhonenumber)
                .eq(SysUser::getPhonenumber, user.getPhonenumber()).last("limit 1"));
        if (Validator.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkEmailUnique(SysUser user) {
        Long userId = Validator.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = getOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getEmail)
                .eq(SysUser::getEmail, user.getEmail()).last("limit 1"));
        if (Validator.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional
    public int insertUser(SysUser user, List<Long> roles, List<Long> groupIds, DataDirDto tree, List<Long> menus) {

        int rows = baseMapper.insert(user);
        if (groupIds != null && tree != null) {
            // 新增用户分组
            insertUserGroup(groupIds, user.getUserId());
            // 默认设置权限
//            loadDefaultTable(tree,1);
            // 添加用户关联表
            insertUserTable(user.getUserId(), tree, null);
        }
        // 新增用户与角色管理
        insertUserRole(roles, user.getUserId());
        //新增用户单独权限
        insertUserMenu(menus, user.getUserId());
        //处理客户信息
        if (user.getUserInfoType() == 3) {
            SysCustomer sysCustomer = user.getSysCustomer();
            sysCustomer.setUserId(user.getUserId());
            sysCustomer.setStatus(1);
            sysCustomer.setCreateBy(SecurityUtils.getUserId());
            sysCustomer.setCreateTime(new Date());
            sysCustomerService.insert(sysCustomer);
        }

        return rows;
    }

    private void loadDefaultTable(DataDirDto tree, int status) {
        if (!ObjectUtils.isEmpty(tree.getAccessType()) && tree.getAccessType() != status) {
            return;
        }
        if (tree.getId() == 0 || "根节点".equals(tree.getDirName())) {
            tree.getChildren().parallelStream().forEach(i -> loadDefaultTable(i, status));
            return;
        }
        tree.setAccessType(status);
        tree.getChildren().parallelStream().forEach(i -> loadDefaultTable(i, status));
    }

    @Override
    public void insertUserTable(Long userId, DataDirDto tree, Integer defaultAccessType) {
        if (Objects.isNull(tree)) return;
        List<SysUserTable> tableList = new ArrayList<>();
        findTable(userId, tree, tableList, defaultAccessType);
        if (!tableList.isEmpty()) {
            userTableService.saveBatch(tableList);
        }
    }

    private void findTable(Long userId, DataDirDto tree, List<SysUserTable> tableList, Integer defaultAccessType) {
        tree.getChildren().forEach(t -> {
            if (t.getIsTable()) {
                SysUserTable table = new SysUserTable();
                table.setAccessType(defaultAccessType != null ? defaultAccessType : t.getAccessType());
                table.setTableId(t.getId());
                table.setUserId(userId);
                table.setDirId(tree.getId());
                //0为无权限，不用保存
                if (table.getAccessType() != null && table.getAccessType() > 0) {
                    tableList.add(table);
                }
            } else if (!ObjectUtils.isEmpty(t.getChildren())) {
                findTable(userId, t, tableList, defaultAccessType);
            }
        });
    }

    /**
     * 修改保存用户信息
     *
     * @param dto 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(UserCreateDto dto) {

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        if (StringUtils.isEmpty(dto.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        }

        SysUser existingUser = this.getOne(Wrappers.<SysUser>query().eq("user_name", dto.getUserName()));

        if (existingUser != null && !existingUser.getUserId().equals(dto.getUserId())) {
            throw new CustomException("用户名已存在", 400);
        }

        List<Long> groupIds = groupService.getGroupIdByLeaderId(user.getUserId());
        if (!ObjectUtils.isEmpty(groupIds)) {
            if (dto.getGroupIds() == null || !dto.getGroupIds().containsAll(groupIds)) {
                throw new CustomException("分组负责人不能直接退出分组");
            }

        }

        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        insertUserRole(dto.getRoleIds(), userId);


        userGroupService.remove(new LambdaQueryWrapper<SysUserGroup>().eq(SysUserGroup::getUserId, userId));
        insertUserGroup(dto.getGroupIds(), userId);
        userTableService.remove(new LambdaQueryWrapper<SysUserTable>().eq(SysUserTable::getUserId, userId));
        insertUserTable(user.getUserId(), dto.getTree(), null);

        //刷新表权限
        this.getUserTableFromCache(userId, true);

        sysUserMenuMapper.delete(new LambdaQueryWrapper<SysUserMenu>().eq(SysUserMenu::getUserId, userId));
        insertUserMenu(dto.getMenus(), userId);

        //更新客户信息
        if (user.getUserInfoType() == 3) {
            sysCustomerService.update(user.getSysCustomer());
        }

        return baseMapper.updateById(user);
    }


    @Override
    public int updateFromTg(UserCreateDto dto) {

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        if (StringUtils.isEmpty(dto.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        }

        SysUser existingUser = this.getOne(Wrappers.<SysUser>query().eq("user_name", dto.getUserName()));

        if (existingUser != null && !existingUser.getUserId().equals(dto.getUserId())) {
            throw new CustomException("用户名已存在", 400);
        }

        //更新客户信息
        if (user.getUserInfoType() == 3) {
            sysCustomerService.update(user.getSysCustomer());
        }

        return baseMapper.updateById(user);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return baseMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .set(SysUser::getAvatar, avatar)
                        .eq(SysUser::getUserName, userName)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return baseMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .set(SysUser::getPassword, password)
                        .eq(SysUser::getUserName, userName));
    }

    /**
     * 新增用户角色信息
     */
    public void insertUserRole(List<Long> roles, Long userId) {
        if (Validator.isNotNull(roles)) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<>();
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0) {
                for (SysUserRole sysUserRole : list) {
                    userRoleMapper.insert(sysUserRole);
                }
            }
        }
    }

    public void insertUserGroup(List<Long> groupIds, Long userId) {
        if (Validator.isNotNull(groupIds)) {
            // 新增用户与角色管理
            List<SysUserGroup> list = new ArrayList<>();
            for (Long id : groupIds) {
                SysUserGroup ur = new SysUserGroup();
                ur.setUserId(userId);
                ur.setGroupId(id);
                list.add(ur);
            }
            if (list.size() > 0) {
                userGroupService.saveBatch(list);
            }
        }
    }


    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        return baseMapper.deleteById(userId);
    }

    @Override
    public List<SysUser> allSyncUser() {
        final LambdaQueryWrapper<SysUser> wq = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDelFlag, 0)
                .eq(SysUser::getUserInfoType, 2)
                .eq(SysUser::getStatus, 0);
        return baseMapper.selectList(wq);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds) {

        List<Long> ids = Arrays.asList(userIds);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public SysUser findByOrgUserId(String orgUserId) {
        final LambdaQueryWrapper<SysUser> eq = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getOrgUserId, orgUserId)
                .eq(SysUser::getDelFlag, 0)
                .eq(SysUser::getUserInfoType, 2);
        return baseMapper.selectOne(eq);
    }

    @Override
    public List<SysUser> getGroupMember(Long groupId) {
        return this.baseMapper.getGroupMember(groupId);
    }

    @Override
    public void updateLoginTime(Long userId, Date now) {

        this.baseMapper.updateLoginTime(userId, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, now));
    }

    @Override
    public HomeUserInfoDto getUserInfo() {
        HomeUserInfoDto homeUserInfoDto = new HomeUserInfoDto();
        LambdaQueryWrapper<SysUser> lqwSU = new LambdaQueryWrapper<>();
        lqwSU.eq(SysUser::getUserId, SecurityUtils.getUserId());
        SysUser sysUser = this.baseMapper.selectOne(lqwSU);
        homeUserInfoDto.setCurrentUserName(sysUser.getUserName());
        homeUserInfoDto.setCurrentRealName(sysUser.getRealName());
        homeUserInfoDto.setCurrentLoginNum(sysUser.getLoginTimes());
        homeUserInfoDto.setToken(sysUser.getToken());
        List<SysUserRole> sysUserRoleList = userRoleMapper.getUserRoleName(Collections.singletonList(sysUser.getUserId()));
        if (!CollectionUtils.isEmpty(sysUserRoleList)) {
            homeUserInfoDto.setCurrentRoles(sysUserRoleList.stream().map(SysUserRole::getRoleName).collect(Collectors.toSet()));
        }
        List<IdAndName> idAndNameList = userGroupService.selectGroupNameByUserId(Collections.singletonList(sysUser.getUserId()));
        if (!CollectionUtils.isEmpty(idAndNameList)) {
            homeUserInfoDto.setCurrentGroups(idAndNameList.stream().map(IdAndName::getName).collect(Collectors.toSet()));
        }
        homeUserInfoDto.setCurrentDataNum(userTableService.getCountTableByUserId(SecurityUtils.getUserId()));
        return homeUserInfoDto;
    }

    @Override
    public Set<Long> getGroupLeaderIdByUserId(Collection<Long> userIds) {
        return baseMapper.getGroupLeaderIdByUserId(userIds);
    }

    @Override
    public Set<Long> getAdmin() {
        return baseMapper.getAdmin();
    }

    @Override
    public void updateLoginTimeToken(String token, Long userId, Date now) {
        this.baseMapper.updateLoginTimeToken(userId, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, now), token);
    }

    @Override
    public SysUser getUserByToken(String token) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(new SysUser()).eq(SysUser::getToken, token));
    }

    @Override
    public int insertSubAccount(SysUser user) {
        int rows = 0;
        if (user.getUserId() == null) {
            rows = baseMapper.insert(user);
        } else {
            SysUser existingUser = this.getOne(Wrappers.<SysUser>query().eq("user_name", user.getUserName()));
            if (existingUser != null && !existingUser.getUserId().equals(user.getUserId())) {
                throw new CustomException("用户名已存在", 400);
            }
            baseMapper.updateById(user);
        }

        return rows;
    }

    @Override
    public SysUser selectUserByPhoneOrEmail(String keyword) {
        return baseMapper.selectUserByPhoneOrEmail(keyword);
    }

    @Override
    public int resetPwdByPhoneOrEmail(String keyword, String password) {
        return baseMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .set(SysUser::getPassword, password)
                        .eq(SysUser::getPhonenumber, keyword)
                        .or().eq(SysUser::getEmail, keyword));
    }

    @Override
    public int selectCntByPhone(String phoneNum) {
        return baseMapper.selectCntByPhone(phoneNum);
    }

    @Override
    public int bindPhoneNum(String username, String phoneNum) {
        return baseMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .set(SysUser::getPhonenumber, phoneNum)
                        .eq(SysUser::getUserName, username));
    }

    @Override
    public int checkOrgIdUnique(String orgId) {
        return baseMapper.selectCntByOrgId(orgId);
    }

    @Override
    public void handleQuitUser() {
        log.info("-----------------------------------处理离职员工信息开始-----------------------------------");
        List<SysUser> list = baseMapper.selectAllUserForQuit();
        for (SysUser sysUser : list) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
            if (sinoPassUserDTO != null) {
                if ("离职".equals(sinoPassUserDTO.getEmployeeStatusText())) {
                    this.update(Wrappers.<SysUser>update().eq("user_id", sysUser.getUserId()).set("status", 1));
                }
            }
        }
    }

    @Override
    public List<UserQuitDto> selectQuitUsers(String realName) {
        List<SysUser> list = baseMapper.selectInternalStaff(realName);
        List<UserQuitDto> userQuitDtos = new ArrayList<>();
        for (SysUser sysUser : list) {
            if (StringUtils.isNotEmpty(sysUser.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                if (sinoPassUserDTO != null) {
                    //列表只展示员工状态为离职，并且 负责表数>0或负责审核数>0或负责项目数>0或负责客户数>0
                    if ("离职".equals(sinoPassUserDTO.getEmployeeStatusText())) {
                        List<Integer> cnts = baseMapper.selectUserAllCnt(sysUser.getUserName(), sysUser.getUserId());
                        if (cnts != null && cnts.size() == 4 && (cnts.get(0) > 0 || cnts.get(1) > 0 || cnts.get(2) > 0 || cnts.get(3) > 0)) {
                            UserQuitDto userQuitDto = new UserQuitDto();
                            BeanUtils.copyProperties(sysUser, userQuitDto);
                            userQuitDto.setTableCnt(cnts.get(0));
                            userQuitDto.setProcessCnt(cnts.get(1));
                            userQuitDto.setProjectCnt(cnts.get(2));
                            userQuitDto.setCustomerCnt(cnts.get(3));
                            userQuitDto.setRealName(sinoPassUserDTO.getViewName());
                            userQuitDtos.add(userQuitDto);
                        }
                    }
                }
            }
        }
        return userQuitDtos;
    }

    @Override
    @Transactional
    public void batchUpdateQuit(List<UserQuitDto> userQuitDtos) {
        //勾选分配后，相当于全部【负责表数】、【负责审核流程】、【负责项目】、【负责客户数】均分配至分配人，分配成功后列表不再展示该条数据
        for (UserQuitDto userQuitDto : userQuitDtos) {
            SysUser sysUser = this.selectUserById(userQuitDto.getNewUserId());
            if (sysUser != null) {
                tableInfoService.update(Wrappers.<TableInfo>update().eq("leader_name", userQuitDto.getUserName()).set("leader_name", sysUser.getUserName()));
            }
            relationTableManageService.update(Wrappers.<TableInfoDiy>update().eq("create_by", userQuitDto.getUserId()).set("create_by", userQuitDto.getNewUserId()));
            sysCustomerService.update(Wrappers.<SysCustomer>update().eq("manage_user", userQuitDto.getUserId()).set("manage_user", userQuitDto.getNewUserId()));
            tgApplicationInfoMapper.updateApplicantId(userQuitDto.getUserId(), userQuitDto.getNewUserId());
            auditProcessService.extendsAuditProcess(userQuitDto.getUserId(), userQuitDto.getNewUserId());

        }
    }

    /**
     * 新增用户菜单信息
     */
    private void insertUserMenu(List<Long> menus, Long userId) {
        if (Validator.isNotNull(menus)) {
            // 新增用户菜单信息
            List<SysUserMenu> list = new ArrayList<>();
            for (Long menuId : menus) {
                SysUserMenu ur = new SysUserMenu();
                ur.setUserId(userId);
                ur.setMenuId(menuId);
                list.add(ur);
            }
            if (list.size() > 0) {
                for (SysUserMenu sysUserMenu : list) {
                    sysUserMenuMapper.insert(sysUserMenu);
                }
            }
        }
    }

    /**
     * MDM
     *
     * @return 部门+姓名
     */
    @Override
    public String getUserViewName(Long... userIds) {
        StringBuilder sb = new StringBuilder();
        for (Long userId : userIds) {
            SysUser user = selectUserById(userId);
            if (user != null && StringUtils.isNotBlank(user.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                if (StringUtils.isNotBlank(sinoPassUserDTO.getViewName())) {
                    sb.append(sinoPassUserDTO.getViewName());
                    sb.append(",");
                }
            }
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * MDM
     *
     * @return 姓名
     */
    public String getUserOnlyName(Long... userIds) {
        StringBuilder sb = new StringBuilder();
        for (Long userId : userIds) {
            SysUser user = selectUserById(userId);
            if (user != null && StringUtils.isNotBlank(user.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                if (StringUtils.isNotBlank(sinoPassUserDTO.getUserName())) {
                    sb.append(sinoPassUserDTO.getUserName());
                    sb.append(",");
                }
            }
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public Long getUserIdByRealName(String realName) {
        SysUser sysUser = baseMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRealName, realName)
                .eq(SysUser::getStatus, 0)
                .eq(SysUser::getDelFlag, 0).last(" limit 1")
        );

        return Optional.ofNullable(sysUser).map(SysUser::getUserId).orElse(null);
    }

    @Override
    public Map<String, Long> getUserIdByRealNames(Collection<String> realName) {
        if (CollectionUtils.isEmpty(realName)) {
            return Collections.emptyMap();
        }
        List<SysUser> users = baseMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .in(SysUser::getRealName, realName)
                .eq(SysUser::getStatus, 0)
                .eq(SysUser::getDelFlag, 0)
        );
        return Lambda.buildMap(users, SysUser::getRealName, SysUser::getUserId);
    }

    @Override
    public String getUserViewName(Collection<SysUser> users) {
        StringBuilder sb = new StringBuilder();
        for (SysUser user : users) {
            if (user != null && StringUtils.isNotBlank(user.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                if (StringUtils.isNotBlank(sinoPassUserDTO.getViewName())) {
                    sb.append(sinoPassUserDTO.getViewName());
                    sb.append(",");
                }
            }
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public List<SysMenu> queryFastEntryInfo(Map<String, Object> parameterMap) {
        List<SysMenu> result = baseMapper.queryFastEntryInfo(parameterMap);

        result.stream().forEach(m -> m.setName(StringUtils.upperCaseFirst(StringUtils.isNotBlank(m.getPath()) ? m.getPath() : "")));
        return result;
    }


}
