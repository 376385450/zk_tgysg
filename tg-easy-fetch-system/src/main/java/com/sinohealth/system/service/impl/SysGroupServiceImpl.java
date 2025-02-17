package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.GroupDataDir;
import com.sinohealth.system.domain.SysUserGroup;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.dto.GroupMemberDto;
import com.sinohealth.system.dto.UserGroupCreateDto;
import com.sinohealth.system.dto.UserGroupDto;
import com.sinohealth.system.mapper.SysGroupMapper;
import com.sinohealth.system.mapper.SysUserGroupMapper;
import com.sinohealth.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.sinohealth.common.core.page.TableSupport.startPage;

/**
 * 用户分组Service业务层处理
 *
 * @author jingjun
 * @date 2021-04-16
 */
@Service
public class SysGroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements ISysGroupService {

    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;
    @Autowired
    private IGroupDataDirService groupDataDirService;

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private IDataDirService dataDirService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysUserTableService userTableService;
    @Autowired
    private ISysRoleService roleService;

    @Override
    @Transactional
    public int insertSysGroup(UserGroupCreateDto dto) {

        SysGroup group = new SysGroup();
        Date now = new Date();
        group.setCreateTime(now);
        List<GroupDataDir> groupDataDirList = new ArrayList<>(dto.getDataDirIds().size());
        dto.getDataDirIds().stream().map(d -> {
            GroupDataDir dir = new GroupDataDir();
            dir.setDirId(d);
            dir.setGroupId(group.getId());
            return dir;
        }).collect(Collectors.toList());
        return this.baseMapper.insert(group);
    }

    @Override
    public List<UserGroupDto> queryList(String name) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<Long> roleids = roleService.selectRoleListByUserId(loginUser.getUserId());
        startPage(" id desc ");
        List<UserGroupDto> list = this.baseMapper.queryList(name, roleids.stream().anyMatch(aLong -> aLong.equals(1L)) ? null : loginUser.getUserId());
        if (!ObjectUtils.isEmpty(list)) {
            List<UserGroupDto> countList = sysUserGroupMapper.countUserGroup(list.stream().map(d -> d.getGroupId()).collect(Collectors.toList()));
            countList.forEach(i -> {
                list.stream().filter(d -> d.getGroupId().equals(i.getGroupId())).findFirst().ifPresent(dto -> {
                    dto.setCountMember(i.getCountMember());
                });
            });
        }

        return list;
    }

    @Override
    public UserGroupCreateDto detail(Long id) {
        SysGroup group = this.baseMapper.selectById(id);
        UserGroupCreateDto dto = new UserGroupCreateDto();
        dto.setGroupId(group.getId());
        dto.setUserId(group.getGroupLeaderId());
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        List<Long> ids = new ArrayList<>();
        ids.add(group.getId());
        dto.setDataDirIds(groupDataDirService.getDirId(ids));
        return dto;
    }


    @Override
    @Transactional
    public void create(UserGroupCreateDto dto) {
        SysGroup group = new SysGroup();
        group.setGroupName(dto.getGroupName());
        group.setDescription(dto.getDescription());
        group.setGroupLeaderId(dto.getUserId());
        group.setStatus(1);
        group.setCreateTime(new Date());
        group.setCreateUserId(SecurityUtils.getLoginUser().getUser().getUserId());
        this.save(group);

        groupDataDirService.saveBatch(dto.getDataDirIds().stream().map(id -> {
            GroupDataDir gd = new GroupDataDir();
            gd.setDirId(id);
            gd.setGroupId(group.getId());
            return gd;
        }).collect(Collectors.toList()));

        if (dto.getUserId() != null) {
            SysUserGroup ug = new SysUserGroup();
            ug.setUserId(dto.getUserId());
            ug.setGroupId(group.getId());
            sysUserGroupMapper.insert(ug);

            userService.insertUserTable(dto.getUserId(), dataDirService.getGroupTreeByDirIds(dto.getDataDirIds(), true, null, false), 5);
        }
    }

    @Override
    @Transactional
    public void edit(UserGroupCreateDto dto) {

        SysGroup g = this.getById(dto.getGroupId());

        if (!g.getGroupLeaderId().equals(dto.getUserId())) {
            SysUserGroup existingUserGroup = sysUserGroupMapper.selectOne(Wrappers.<SysUserGroup>query().eq("group_id", dto.getGroupId()).eq("user_id", dto.getUserId()));
            if (existingUserGroup == null) {
                SysUserGroup userGroup = new SysUserGroup();
                userGroup.setGroupId(dto.getGroupId());
                userGroup.setUserId(dto.getUserId());
                sysUserGroupMapper.insert(userGroup);
            }
        }
        SysGroup group = new SysGroup();
        group.setId(dto.getGroupId());
        group.setGroupName(dto.getGroupName());
        group.setDescription(dto.getDescription());
        group.setGroupLeaderId(dto.getUserId());
        group.setUpdateTime(new Date());
        group.setUpdateUserId(SecurityUtils.getLoginUser().getUser().getUserId());
        this.updateById(group);

        QueryWrapper<GroupDataDir> wrapper = Wrappers.query();
        wrapper.eq("group_id", dto.getGroupId());
        groupDataDirService.remove(wrapper);

        groupDataDirService.saveBatch(dto.getDataDirIds().stream().map(id -> {
            GroupDataDir gd = new GroupDataDir();
            gd.setDirId(id);
            gd.setGroupId(group.getId());
            return gd;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<SysUser> getMembers(Long groupId) {
        return userService.getGroupMember(groupId);
    }

    @Override
    @Transactional
    public void updateMembers(GroupMemberDto dto) {

        List<SysUserGroup> userGroups = sysUserGroupMapper.getByGroupId(dto.getGroupId());
        if (dto.getIsAdd()) {
            if (!ObjectUtils.isEmpty(userGroups)) {
                for (Long userId : dto.getUserIds()) {
                    if (!userGroups.stream().anyMatch(u -> u.getUserId().equals(userId))) {

                        List<Long> existingDirIds = groupDataDirService.getDirIdByUserId(userId);
                        List<Long> groupDirIds = groupDataDirService.getDirId(Arrays.asList(dto.getGroupId()));
                        groupDirIds = groupDirIds.stream().filter(g -> !existingDirIds.stream().anyMatch(id -> id.equals(g))).collect(Collectors.toList());
                        if (!groupDirIds.isEmpty()) {
                            List<TableInfo> list = tableInfoService.list(Wrappers.<TableInfo>query().select("id", "dir_id").in("dir_id", groupDirIds).eq("status", 1));

                            if (!ObjectUtils.isEmpty(list)) {//添加用户 表权限
                                userTableService.saveBatch(list.stream().map(t -> {
                                    SysUserTable ut = new SysUserTable();
                                    ut.setUserId(userId);
                                    ut.setDirId(t.getDirId());
                                    ut.setAccessType(1);
                                    ut.setTableId(t.getId());
                                    return ut;
                                }).collect(Collectors.toList()));
                            }
                        }
                        SysUserGroup userGroup = new SysUserGroup();
                        userGroup.setGroupId(dto.getGroupId());
                        userGroup.setUserId(userId);
                        sysUserGroupMapper.insert(userGroup);


                    }
                }
            }
        } else {

            SysGroup g = this.getById(dto.getGroupId());

            dto.getUserIds().stream().filter(u -> u.equals(g.getGroupLeaderId())).findFirst().ifPresent(u -> {
                throw new CustomException("不能刪除分组负责人！");
            });
            for (Long userId : dto.getUserIds()) {
                userGroups.stream().filter(u -> u.getUserId().equals(userId)).findFirst().ifPresent(u -> {
                    sysUserGroupMapper.deleteById(u.getId());
                    List<Long> existingDirIds = groupDataDirService.getDirIdByUserId(userId);
                    List<Long> groupDirIds = groupDataDirService.getDirId(Arrays.asList(dto.getGroupId()));
                    groupDirIds = groupDirIds.stream().filter(gd -> !existingDirIds.stream().anyMatch(id -> id.equals(gd))).collect(Collectors.toList());
                    if (!groupDirIds.isEmpty()) {
                        userTableService.remove(Wrappers.<SysUserTable>query().eq("user_id", userId).in("dir_id", groupDirIds));
                    }
                });
            }
        }
    }

    @Override
    public List<UserGroupDto> getUserGroups(String name) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.isAdmin()) {
            return this.baseMapper.queryList(name, null);
        } else {
            return this.baseMapper.queryList(name, loginUser.getUserId());
        }


    }

    @Override
    public List<Long> getGroupIdByLeaderId(Long leaderId) {
        return this.baseMapper.getGroupIdByLeaderId(leaderId);
    }


}
