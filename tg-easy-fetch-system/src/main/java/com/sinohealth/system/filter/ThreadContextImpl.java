package com.sinohealth.system.filter;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.core.domain.entity.SysUser;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Rudolph
 * @Date 2022-04-29 9:47
 * @Desc
 */

public class ThreadContextImpl implements ThreadContext{

    private SysUser user;

    private Map params;


    @Override
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


    @Override
    public Map getParams() {
        if (ObjectUtils.isNotNull(this.params)) {
            return this.params;
        } else {
            setParams(new HashMap<>());
            return this.params;
        }

    }

    @Override
    public void setSysUser(SysUser user) {
        this.user = user;
    }

    @Override
    public SysUser getSysUser() {
        return this.user;
    }

}
