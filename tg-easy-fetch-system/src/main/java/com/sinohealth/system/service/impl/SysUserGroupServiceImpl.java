package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.system.domain.SysUserGroup;
import com.sinohealth.system.dto.ApiServiceInvokeDto;
import com.sinohealth.system.dto.common.IdAndName;
import com.sinohealth.system.mapper.SysUserGroupMapper;
import com.sinohealth.system.service.ISysUserGroupService;
import com.sinohealth.system.vo.ApiServiceQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 用户组关联Service业务层处理
 * 
 * @author jingjun
 * @date 2021-04-16
 */
@Service
public class SysUserGroupServiceImpl extends ServiceImpl<SysUserGroupMapper, SysUserGroup> implements ISysUserGroupService
{
    @Override
    public List<IdAndName> selectGroupNameByUserId(Collection<Long> userIds) {
        return baseMapper.selectGroupNameByUserId(userIds);
    }
}
