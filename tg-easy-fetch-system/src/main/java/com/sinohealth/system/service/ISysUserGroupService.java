package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.system.domain.SysUserGroup;
import com.sinohealth.system.dto.common.IdAndName;
import com.sinohealth.system.vo.ApiServiceQueryVo;

import java.util.Collection;
import java.util.List;

/**
 * 用户组关联Service接口
 * 
 * @author jingjun
 * @date 2021-04-16
 */
public interface ISysUserGroupService  extends IService<SysUserGroup>
{

    List<IdAndName> selectGroupNameByUserId(Collection<Long> userIds);
}
