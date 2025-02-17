package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.annotation.Log;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.UserConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysRole;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.enums.BusinessType;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.system.domain.SysUserRole;
import com.sinohealth.system.dto.PasswordDto;
import com.sinohealth.system.dto.UserCreateDto;
import com.sinohealth.system.dto.UserQuitDto;
import com.sinohealth.system.mapper.SysUserRoleMapper;
import com.sinohealth.system.service.ISysRoleService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.web.generate.user.UserApiGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息
 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@Api(tags = {"用户管理"})
public class SysUserApiController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private UserApiGenerator userApiGenerator;

    @Autowired
    private ISysRoleService roleService;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 获取用户列表
     */
//// @PreAuthorize("@ss.hasAnyRoles('data_analyist,business_analysist,inner_user,data_manager,business_manager,system_manager') || @ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    @ApiOperation("用户列表")
    public TableDataInfo list(@RequestParam(required = false) String userName,
                              @RequestParam(required = false) Integer roleId,
                              @RequestParam(required = false) Integer status,
                              @RequestParam(defaultValue = "1", required = false) Integer pageNum,
                              @RequestParam(defaultValue = "10000", required = false) Integer pageSize) {
        List<SysUser> list = userService.selectList(userName, roleId, status, pageNum, pageSize);
        return getDataTable(list);
    }

    /**
     * 根据用户编号获取详细信息
     */
//// @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    @ApiOperation("用户详情")
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SecurityUtils.getLoginUser().isAdmin() ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (StringUtils.isNotNull(userId)) {
            UserCreateDto dto = userService.getUserDetail(userId);
            ajax.put(AjaxResult.DATA_TAG, dto);
            ajax.put("roleIds", dto.getRoleIds());
        }
        return ajax;
    }


    @GetMapping(value = "/system_init")
    public AjaxResult initSystemUser() {

        Map<String, List<String>> stringListMap = null;
        try {
            stringListMap = userApiGenerator.initUserInfo();
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        }
        return new AjaxResult<>(200, "请求成功", stringListMap);
    }

    /**
     * 新增用户 新增客户
     */
//// @PreAuthorize("@ss.hasPermi('system:user:add')")
    @PostMapping
    @ApiOperation(value = "创建用户")
    public AjaxResult add(@Validated @RequestBody UserCreateDto dto) {
        if (dto.getUserInfoType().equals(CommonConstants.EMPLOYEE) && StringUtils.isBlank(dto.getOrgUserId())) {
            return AjaxResult.error("内部员工必须提供组织用户信息");
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(dto.getUserName()))) {
            return AjaxResult.error("新增用户'" + dto.getUserName() + "'失败，登录账号已存在");
        }

        if (dto.getUserInfoType() != 3 && userService.checkOrgIdUnique(dto.getOrgUserId()) > 0) {
            return AjaxResult.error("新增用户'" + dto.getRealName() + "'失败，员工已存在");
        }

        if ("离职".equals(dto.getEmployeeStatusText())) {
            return AjaxResult.error("新增用户'" + dto.getRealName() + "'员工已离职");
        }

        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        user.setToken(UUID.fastUUID().toString());
        userService.insertUser(user, dto.getRoleIds(), dto.getGroupIds(), dto.getTree(), dto.getMenus());
        return toAjax(1);
    }

    //// @PreAuthorize("@ss.hasPermi('system:user:add')")

    @GetMapping("/status")
    @ApiOperation(value = "停用用户")
    public AjaxResult add(@ApiParam(value = "0恢复 1停用") @RequestParam int status, @RequestParam Long userId) {
        userService.update(Wrappers.<SysUser>update().eq("user_id", userId).set("status", status));
        return AjaxResult.success();
    }

    /**
     * 修改用户
     */
//// @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("编辑用户")
    public AjaxResult edit(@Validated @RequestBody UserCreateDto dto) {
        if (dto.getUserInfoType().equals(CommonConstants.EMPLOYEE) && StringUtils.isBlank(dto.getOrgUserId())) {
            return AjaxResult.error("内部员工必须提供组织用户ID");
        }
        return toAjax(userService.updateUser(dto));
    }

    /**
     * 删除用户
     */
//// @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
//// @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    @ApiOperation("修改密码")
    public AjaxResult resetPwd(@RequestBody PasswordDto dto) {
        SysUser user = new SysUser();
        user.setUserId(dto.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
//// @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 管理子账号
     */

    @PostMapping("/addSubAccount")
    @ApiOperation(value = "管理子账号")
    public AjaxResult addSubAccount(@Validated @RequestBody UserCreateDto dto) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        if (dto.getUserId() == null && UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(dto.getUserName()))) {
            return AjaxResult.error("新增用户'" + dto.getUserName() + "'失败，登录账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
        user.setToken(UUID.fastUUID().toString());
        user.setIsSubAccount(1);
        int rows = userService.insertSubAccount(user);
        if (rows != 0) {
            List<SysUserRole> parentRole = userRoleMapper.selectList(new QueryWrapper<SysUserRole>()
                    .lambda().eq(SysUserRole::getUserId, dto.getParentAccountId()));
            if (CollectionUtils.isNotEmpty(parentRole)) {
                log.info(": or={}", parentRole);
                parentRole.stream().peek(v -> v.setUserId(user.getUserId())).forEach(userRoleMapper::insert);
            }
        }
        return toAjax(1);
    }

    /**
     * 获企微用户列表
     */

    @GetMapping("/enterpriseUserList")
    @ApiOperation("企微用户列表")
    public TableDataInfo enterpriseUserList(@RequestParam(required = false) String userName,
                                            @RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize) {
        startPage();
        List<SinoPassUserDTO> list = SinoipaasUtils.employeeSelectbypage(userName, pageNum, pageSize);
        if (list == null) {
            list = new ArrayList<>();
        }
        return getDataTable(list);
    }

    /**
     * 重置密码
     */

    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwdByPhoneOrEmail")
    @ApiOperation("验证码重置密码")
    public AjaxResult resetPwdByPhoneOrEmail(@RequestBody PasswordDto dto) {
        return toAjax(userService.resetPwdByPhoneOrEmail(dto.getKeyword(), SecurityUtils.encryptPassword(dto.getPassword())));
    }


    @GetMapping("/bindPhoneByUsername")
    @ApiOperation("绑定手机账号")
    public AjaxResult bindPhoneByUsername(@RequestParam("username") String username, @RequestParam("phoneNum") String phoneNum) {
        return toAjax(userService.bindPhoneNum(username, phoneNum));
    }


    @GetMapping("/quitUsers")
    @ApiOperation(value = "离职员工列表", response = UserQuitDto.class)
    public TableDataInfo selectQuitUsers(@RequestParam(required = false) String realName) {
        //startPage();
        List<UserQuitDto> list = userService.selectQuitUsers(realName);
        if (list == null) {
            list = new ArrayList<>();
        }
        return getDataTable(list);
    }


    @PostMapping("/batchUpdateQuit")
    @ApiOperation(value = "离职分配")
    public AjaxResult batchUpdateQuit(@Validated @RequestBody List<UserQuitDto> userQuitDtos) {
        userService.batchUpdateQuit(userQuitDtos);
        return AjaxResult.success();
    }
}
