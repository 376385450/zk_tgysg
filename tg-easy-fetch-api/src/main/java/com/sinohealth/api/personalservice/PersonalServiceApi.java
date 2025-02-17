package com.sinohealth.api.personalservice;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.personalservice.JudgePermissionRequest;
import com.sinohealth.system.dto.personalservice.PageQueryServiceRequest;
import com.sinohealth.system.dto.personalservice.QueryPermissionListRequest;
import com.sinohealth.system.vo.PersonalServiceVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 10:37
 */
@RequestMapping({"/api/personal_service"})
public interface PersonalServiceApi {

    @PostMapping("/pageQuery")
    AjaxResult<PageInfo<PersonalServiceVo>> pageQuery(@RequestBody @Validated PageQueryServiceRequest pageRequest);

    @PostMapping("/pagePermission")
    AjaxResult<List<String>> queryPermissionList(QueryPermissionListRequest queryPermissionListRequest);

    @PostMapping("/judgePermission")
    AjaxResult<Object> judgePermission(@RequestBody @Validated JudgePermissionRequest judgePermissionRequest);
}
