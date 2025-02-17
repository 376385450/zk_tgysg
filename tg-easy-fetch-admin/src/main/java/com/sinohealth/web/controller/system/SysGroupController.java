package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysGroup;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.dto.GroupMemberDto;
import com.sinohealth.system.dto.UserGroupCreateDto;
import com.sinohealth.system.dto.UserGroupDto;
import com.sinohealth.system.service.ISysGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author Jingjun
 * @since 2021/4/15
 */
@RestController
@RequestMapping("/system/group")
@Api(tags = {"用户分组"})
public class SysGroupController extends BaseController {

    @Autowired
    private ISysGroupService groupService;

    @GetMapping("/list")
    //@ApiOperation("列表")
    public TableDataInfo list(@RequestParam(required = false) String name) {
        return getDataTable(groupService.queryList(name));
    }

    @GetMapping()
    //@ApiOperation(value = "登录用户所有分组列表",response = UserGroupDto.class)
    public AjaxResult getMyGroup(@RequestParam(required = false) String name) {
        return AjaxResult.success(groupService.getUserGroups(name));
    }

    @PostMapping("/add")
    //@ApiOperation("添加分组")
    public AjaxResult add(@RequestBody UserGroupCreateDto userGroupCreateDto) {
        groupService.create(userGroupCreateDto);
        return AjaxResult.success();
    }

    @GetMapping("/{id}")
    //@ApiOperation("详情")
    public AjaxResult list(@PathVariable Long id) {
        return AjaxResult.success(groupService.detail(id));
    }


    @GetMapping("/{id}/members")
    //@ApiOperation(value = "成员列表", response = SysUser.class)
    public AjaxResult members(@PathVariable Long id) {
        return AjaxResult.success(groupService.getMembers(id));
    }


    @PutMapping("/members")
    //@ApiOperation("编辑成员")
    public AjaxResult updateMembers(@Validated  @RequestBody GroupMemberDto dto) {
        groupService.updateMembers(dto);
        return AjaxResult.success();
    }


    @PutMapping("/edit")
    //@ApiOperation("编辑分组")
    public AjaxResult edit(@RequestBody UserGroupCreateDto userGroupCreateDto) {
        groupService.edit(userGroupCreateDto);
        return AjaxResult.success();
    }

    @DeleteMapping("/{groupIds}")
    public AjaxResult remove(@PathVariable Long[] groupIds) {
        groupService.update(Wrappers.<SysGroup>update().in("group_id", groupIds).set("status", 0).set("update_time", new Date()).set("update_user_id", SecurityUtils.getUserId()));
        return AjaxResult.success();
    }
}
