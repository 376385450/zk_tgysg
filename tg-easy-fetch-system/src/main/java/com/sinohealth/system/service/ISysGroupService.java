package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.system.dto.GroupMemberDto;
import com.sinohealth.system.dto.UserGroupCreateDto;
import com.sinohealth.system.dto.UserGroupDto;

import java.util.List;

/**
 * 用户分组Service接口
 * 
 * @author jingjun
 * @date 2021-04-16
 */
public interface ISysGroupService extends IService<SysGroup>
{
    public int insertSysGroup(UserGroupCreateDto dto);

    public List<UserGroupDto> queryList(String name);

    public UserGroupCreateDto detail(Long id);

    public void create(UserGroupCreateDto dto);

    public void edit(UserGroupCreateDto dto);

    public List<SysUser> getMembers(Long groupId);

    public void updateMembers(GroupMemberDto dto);

    public List<UserGroupDto> getUserGroups(String name);

    public List<Long> getGroupIdByLeaderId(Long leaderId);

}
