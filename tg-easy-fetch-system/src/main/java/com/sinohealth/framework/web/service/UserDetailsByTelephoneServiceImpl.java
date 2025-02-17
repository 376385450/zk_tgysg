package com.sinohealth.framework.web.service;

import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.enums.UserStatus;
import com.sinohealth.common.exception.BaseException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.service.ISysRoleService;
import com.sinohealth.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户验证处理
 *
 * @author gmk
 */
@Service("userDetailsByTelephoneService")
public class UserDetailsByTelephoneServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsByTelephoneServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysRoleService roleService;


    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByPhoneOrEmail(phone);
        if (StringUtils.isNull(user)) {
            log.info("登录手机号：{} 不存在.", phone);
            throw new UsernameNotFoundException("登录手机号：" + phone + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录手机号：{} 已被删除.", phone);
            throw new BaseException("对不起，您的手机号：" + phone + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录手机号：{} 已被停用.", phone);
            throw new BaseException("对不起，您的手机号：" + phone + " 已停用");
        }

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user) {
        List<Long> roleids= roleService.selectRoleListByUserId(user.getUserId());
        return new LoginUser(user, permissionService.getMenuPermission(user,roleids.stream().anyMatch(id->id<2L)),roleids);
    }
}
