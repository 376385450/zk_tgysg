package com.sinohealth.system.filter;

import com.sinohealth.common.core.domain.entity.SysUser;

import java.util.Map;

/**
 * @Author Rudolph
 * @Date 2022-04-29 9:47
 * @Desc
 */
public interface ThreadContext {

    void setParams(Map<String, Object> params);

    Map getParams();

    void setSysUser(SysUser user);

    SysUser getSysUser();


}
