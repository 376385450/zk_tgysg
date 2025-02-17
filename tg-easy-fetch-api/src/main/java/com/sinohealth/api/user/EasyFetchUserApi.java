package com.sinohealth.api.user;

import com.sinohealth.api.user.dto.UserDeleteDto;
import com.sinohealth.api.user.dto.userSync.SyncUser;
import com.sinohealth.api.user.dto.userSync.TgSyncUserResp;
import com.sinohealth.api.user.dto.UserCreateDto;
import com.sinohealth.api.user.dto.userSync.TgUser;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/4
 */
@RequestMapping("/api/easyfetch/user")
public interface EasyFetchUserApi {

    @PostMapping("/deleteUserMapping")
    AjaxResult deleteUserMapping(@RequestBody UserDeleteDto userDeleteDto);

    @PostMapping("/addUser")
    @ApiOperation(value = "创建用户")
    AjaxResult add(@RequestBody UserCreateDto userCreateDto);

    @PostMapping("/editUser")
    @ApiOperation(value = "更新用户")
    public AjaxResult edit(@Validated @RequestBody UserCreateDto userCreateDto);


    @GetMapping("/list")
    @ApiOperation(value = "获取用户列表")
    AjaxResult getList();

    @GetMapping("/getUnMappingUsers")
    List<SyncUser> getUnMappingUsers();

    @PostMapping("/MappingUsers")
    AjaxResult mappingUsers(@RequestBody List<SyncUser> list);

    @PostMapping("/syncTgUser")
    TgSyncUserResp syncTgUser(@RequestBody List<TgUser> list);


}
