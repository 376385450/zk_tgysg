package com.sinohealth.framework.web.service;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.enums.UserStatus;
import com.sinohealth.common.exception.BaseException;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.ISysRoleService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.MockUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * 用户验证处理
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, MockUserService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new BaseException("对不起，您的账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new BaseException("对不起，您的账号：" + username + " 已停用");
        }

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user) {
        List<Long> roleids = roleService.selectRoleListByUserId(user.getUserId());
        return new LoginUser(user, permissionService.getMenuPermission(user, roleids.stream().anyMatch(id -> id < 2L)), roleids);
    }

    public void fillUserAuthById(HttpServletRequest request, Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        ThreadContextHolder.setSysUser(sysUser);
        UserDetails userDetails = loadUserByUsername(sysUser.getUserName());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        SinoPassUserDTO orgUserInfo;
        try {
            if (ObjectUtils.isNotNull(sysUser.getOrgUserId())) {
                orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId())).orElse(createEmptyOrgUser());
            } else {
                orgUserInfo = createEmptyOrgUser();
            }
        } catch (NullPointerException e) {
            log.error("异常捕获", e);
            orgUserInfo = createEmptyOrgUser();
        }

        ThreadContextHolder.getParams().put(CommonConstants.ORG_USER_INFO, orgUserInfo);
    }

    private SinoPassUserDTO createEmptyOrgUser() {
        return new SinoPassUserDTO();
    }


}
