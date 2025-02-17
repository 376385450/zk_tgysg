package com.sinohealth.web.controller.user;

import cn.hutool.core.collection.CollUtil;
import com.sinohealth.api.user.EasyFetchUserApi;
import com.sinohealth.api.user.dto.UserCreateDto;
import com.sinohealth.api.user.dto.UserDeleteDto;
import com.sinohealth.api.user.dto.userSync.SyncUser;
import com.sinohealth.api.user.dto.userSync.TgSyncUserResp;
import com.sinohealth.api.user.dto.userSync.TgUser;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.system.domain.TgIntelligenceUserMapping;
import com.sinohealth.system.service.AssetUserService;
import com.sinohealth.system.service.ISysUserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2023/8/5
 */
@Slf4j
@Api(value = "/api/easyfetch/user", tags = {"易数阁用户体系"})
@RestController
@RequestMapping({"/api/easyfetch/user"})
public class AssetUserApiController extends BaseController implements EasyFetchUserApi {

    @Autowired
    AssetUserService assetUserService;

    @Autowired
    ISysUserService userService;


    @Override
    @PostMapping("/deleteUserMapping")
    public AjaxResult deleteUserMapping(@RequestBody UserDeleteDto userDeleteDto) {
        deleteRelationTgYsg(userDeleteDto.getTgUserId(),null);
        return AjaxResult.success();
    }


    @Override
    @PostMapping("/addUser")
    public AjaxResult<Long> add(@RequestBody UserCreateDto dto) {

        final String orgUserId = dto.getOrgUserId();
        final Long tgUserId = dto.getTgUserId();

        Long addUserId = null;

        // 查询是否存在项目org_user_id的用户
        final SysUser byOrgUserId = userService.findByOrgUserId(orgUserId);
        if (Objects.nonNull(byOrgUserId)) {
            // 存在org_user_id已绑定的用户，则绑定id映射
            // tips: 不允许存在用户已绑定其他用户的情况，前期同步数据已限制
            final Long userId = byOrgUserId.getUserId();
            addRelationTgYsg(tgUserId, userId);
        } else {
            SysUser user = new SysUser();
            BeanUtils.copyProperties(dto, user);
            user.setCreateBy(StringUtils.isNotEmpty(dto.getCreator())?dto.getCreator():SecurityUtils.getUsername());
            user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
            user.setToken(UUID.fastUUID().toString());
            userService.insertUser(user, dto.getRoleIds(), dto.getGroupIds(), null, dto.getMenus());
            addUserId = user.getUserId();
            // 保存天宫与用户服务映射
            addRelationTgYsg(dto.getTgUserId(), addUserId);
        }

        return AjaxResult.success(addUserId);
    }

    @Override
    @PostMapping("/editUser")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(UserCreateDto userCreateDto) {

        final String orgUserId = userCreateDto.getOrgUserId();
        final Long tgUserId = userCreateDto.getTgUserId();

        // 需要填充易数阁用户id
        Long editUserId = null;
        // 如果存在映射关系，修改映射关系的用户信息
        // 不存在用户关系，则使用orgUserId查询是否存在用户
        // 存在则修改对应用户，不存在则新增
        final TgIntelligenceUserMapping mapping = assetUserService.findMappingByTgUserId(tgUserId);
        if (Objects.isNull(mapping)) {
            // 如果映射关系不存在，则根据org_user_id查看是否存在
            final SysUser byOrgUserId = userService.findByOrgUserId(orgUserId);
            if (Objects.nonNull(byOrgUserId)) {
                // 存在org_user_id已绑定的用户，则绑定id映射
                // tips: 不允许存在用户已绑定其他用户的情况，前期同步数据已限制
                final Long userId = byOrgUserId.getUserId();
                addRelationTgYsg(tgUserId, userId);
                editUserId = userId;
            } else {
                // 不存在，则新增用户
                final AjaxResult<Long> add = add(userCreateDto);
                // 填充用户id
                editUserId = add.getData();
            }
        } else {
            // 如果映射关系存在，则根据用户id获取用户
            final Long ysgUserId = mapping.getYsgUserId();
            final SysUser sysUser = userService.selectUserById(ysgUserId);
            // 判断org_user_id 是否相同
            if (!sysUser.getOrgUserId().equals(orgUserId)) {
                // 不相同的话，说明天宫换绑用户,查看是否存在新org_user_id绑定的用户（临时逻辑）
                final SysUser byOrgUserId = userService.findByOrgUserId(orgUserId);
                if (Objects.nonNull(byOrgUserId)) {
                    // 存在，则更新映射关系
                    // 1. 删除先用映射
                    deleteRelationTgYsg(ysgUserId, tgUserId);
                    // 2. 保存新映射
                    addRelationTgYsg(byOrgUserId.getUserId(), tgUserId);
                    // 填充用户id
                    editUserId = sysUser.getUserId();
                } else {
                    // 1. 删除原有映射
                    deleteRelationTgYsg(ysgUserId, tgUserId);
                    // 2. 新增用户
                    final AjaxResult<Long> add = add(userCreateDto);
                    // 填充用户id
                    editUserId = add.getData();
                }
            } else {
                // 相同，则直接更新用户信息
                editUserId = ysgUserId;
            }
        }

        userCreateDto.setUserId(editUserId);

        /*return AjaxResult.success(200);*/
        com.sinohealth.system.dto.UserCreateDto createDto = new com.sinohealth.system.dto.UserCreateDto();
        BeanUtils.copyProperties(userCreateDto, createDto);
        return toAjax(userService.updateFromTg(createDto));
    }


    @Override
    @GetMapping("/list")
    public AjaxResult getList() {

        return null;
    }

    @Override
    public List<SyncUser> getUnMappingUsers() {
        final List<SysUser> unMappingUsers = userService.getUnMappingUsers();
        if (CollUtil.isNotEmpty(unMappingUsers)) {
            final List<SyncUser> collect = unMappingUsers.stream()
                    .map(unMappingUser -> {
                        final SyncUser syncUser = user2Sync(null, unMappingUser);
                        syncUser.setOrgUserId(unMappingUser.getOrgUserId());
                        return syncUser;
                    }).collect(Collectors.toList());
            return collect;
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult mappingUsers(List<SyncUser> list) {
        for (SyncUser syncUser : list) {
            addRelationTgYsg(Long.parseLong(syncUser.getTgId()), Long.parseLong(syncUser.getYsgId()));
        }
        return AjaxResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TgSyncUserResp syncTgUser(List<TgUser> list) {

        final TgSyncUserResp tgSyncUserResp = new TgSyncUserResp();

        List<SyncUser> successList = new ArrayList<SyncUser>();

        List<SyncUser> failureList = new ArrayList<SyncUser>();

        // 获取所有已经绑定的用户
        final List<TgIntelligenceUserMapping> mappings = assetUserService.listAllMapping();

        final List<SysUser> sysUsers = userService.allSyncUser();

        final Map<Long, TgIntelligenceUserMapping> mappingMap = mappings.stream().collect(Collectors.toMap(TgIntelligenceUserMapping::getYsgUserId, v -> v));

        final Map<String, List<SysUser>> sysUserMap = sysUsers.stream().collect(Collectors.groupingBy(SysUser::getOrgUserId));

        for (TgUser tgUser : list) {
            // 校验是否已存在mdm关联的用户
            final List<SysUser> sysUsers1 = sysUserMap.get(tgUser.getOrgId());

            SysUser user = null;
            if (CollUtil.isNotEmpty(sysUsers1)) {
                if (sysUsers1.size() == 1) {
                    user = sysUsers1.get(0);
                } else {
                    final SyncUser syncUser = user2Sync(tgUser, null);
                    syncUser.setErrorMsg("mdm用户id：【"+tgUser.getOrgId()+"】在易数阁存在多个用户");
                    failureList.add(syncUser);
                    continue;
                }
            }

            final SyncUser e = user2Sync(tgUser, user);
            if (Objects.isNull(user)) {
                // 新增用户
                final UserCreateDto userCreateDto = new UserCreateDto();
                // 填充用户信息
                userCreateDto.setOrgUserId(tgUser.getOrgId());
                userCreateDto.setPassword(tgUser.getPwd());
                userCreateDto.setUserName(tgUser.getAccount());
                userCreateDto.setRealName(tgUser.getRealName());
                userCreateDto.setUserInfoType(2);
                userCreateDto.setTgUserId(Long.parseLong(tgUser.getId()));
                userCreateDto.setEmail(tgUser.getEmail());
                userCreateDto.setPhonenumber(tgUser.getPhone());
                userCreateDto.setCreator("123");
                // 保存用户
                final AjaxResult<Long> result = add(userCreateDto);
                if (result.isSuccess()) {
                    final Long id = result.getData();
                    e.setYsgId(id.toString());
                    e.setYsgPhone(userCreateDto.getPhonenumber());
                    e.setYsgEmail(userCreateDto.getEmail());
                    e.setYsgAccount(userCreateDto.getUserName());
                    e.setYsgRealName(userCreateDto.getRealName());
                    successList.add(e);
                    continue;
                }
            } else {
                // 查看是否存在映射关系
                final TgIntelligenceUserMapping mapping = mappingMap.get(user.getUserId());
                if (Objects.isNull(mapping)) {
                    // 不存在映射关系，校验姓名，手机号码，邮箱，登录账号是否一致
                    boolean isFail = false;
                    StringBuffer errorMsg = new StringBuffer();
                    if (!user.getRealName().equals(tgUser.getRealName())) {
                        isFail = true;
                        errorMsg.append("姓名,");
                    }
                    if (!user.getPhonenumber().equals(tgUser.getPhone())) {
                        isFail = true;
                        errorMsg.append("手机号码,");
                    }
                    if (!user.getEmail().equals(tgUser.getEmail())) {
                        isFail = true;
                        errorMsg.append("邮箱,");
                    }
                    if (!user.getUserName().equals(tgUser.getAccount())) {
                        isFail = true;
                        errorMsg.append("登录账号,");
                    }

                    if (isFail) {
                        errorMsg.append("不一致");
                        e.setErrorMsg(errorMsg.toString());
                        failureList.add(e);
                        continue;
                    } else {
                        // 新增mapping映射
                        assetUserService.addUserTgRelation(Long.parseLong(tgUser.getId()), user.getUserId());
                        successList.add(e);
                        continue;
                    }

                } else {
                    // 存在映射关系，查看id是否对得上
                    if (mapping.getTgUserId().toString().equals(tgUser.getId())) {
                        // 相同
                        successList.add(e);
                        continue;
                    } else {
                        // 不相同
                        e.setErrorMsg("易数阁已绑定id【" +mapping.getTgUserId()+ "】与匹配id【" +tgUser.getId()+ "】不同");
                        failureList.add(e);
                        continue;
                    }
                }
            }
        }
        tgSyncUserResp.setSuccessList(successList);
        tgSyncUserResp.setFailureList(failureList);

        return tgSyncUserResp;
    }

    private SyncUser user2Sync(TgUser tgUser, SysUser sysUser) {
        final SyncUser syncUser = new SyncUser();
        if (Objects.nonNull(tgUser)) {
            syncUser.setTenantCode(tgUser.getTenantCode());
            syncUser.setTenantName(tgUser.getTenantName());
            syncUser.setOrgUserId(tgUser.getOrgId());
            syncUser.setTgId(tgUser.getId());
            syncUser.setTgRealName(tgUser.getRealName());
            syncUser.setTgAccount(tgUser.getAccount());
            syncUser.setTgEmail(tgUser.getEmail());
            syncUser.setTgPhone(tgUser.getPhone());
        }
        if (Objects.nonNull(sysUser)) {
            syncUser.setYsgAccount(sysUser.getUserName());
            syncUser.setYsgRealName(sysUser.getRealName());
            syncUser.setYsgEmail(sysUser.getEmail());
            syncUser.setYsgId(sysUser.getUserId()+"");
            syncUser.setYsgPhone(sysUser.getPhonenumber());
        }
        return syncUser;
    }

    private void addRelationTgYsg(Long tgUserId, Long ysgUserId) {
        // 保存天宫与用户服务映射
        assetUserService.addUserTgRelation(tgUserId, ysgUserId);
    }

    private void deleteRelationTgYsg(Long tgUserId, Long ysgUserId) {
        assetUserService.deleteUserTgRelation(tgUserId, ysgUserId);
    }

}
