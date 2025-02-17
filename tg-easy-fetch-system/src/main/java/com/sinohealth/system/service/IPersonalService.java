package com.sinohealth.system.service;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.personalservice.JudgePermissionRequest;
import com.sinohealth.system.dto.personalservice.PageQueryServiceRequest;
import com.sinohealth.system.vo.PersonalServiceVo;

import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 10:56
 */
public interface IPersonalService {

    /**
     * 分页查询我的服务
     *
     * @param pageRequest
     * @return
     */
    AjaxResult<PageInfo<PersonalServiceVo>> pageQuery(PageQueryServiceRequest pageRequest);

    /**
     * 根据资产查询权限
     *
     * @param assetId
     * @return
     */
    AjaxResult<List<String>> queryPermissionList(Long assetId);

    /**
     * 判断是否有操作权限
     *
     * @param judgePermissionRequest
     * @return
     */
    AjaxResult<Object> judgePermission(JudgePermissionRequest judgePermissionRequest);
}
