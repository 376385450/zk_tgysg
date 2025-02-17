package com.sinohealth.system.config;

import cn.hutool.crypto.SecureUtil;
import com.sinohealth.common.utils.SecurityUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 自定义密码验证方式
 * 易数阁使用明文传输，天宫使用md5加密，两种加密方式互相兼容（在并行过程中）
 * @Author shallwetalk
 * @Date 2023/9/1
 */
public class TgAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            this.logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        } else {
            // 传入密码
            String presentedPassword = authentication.getCredentials().toString();
            // md5加密
            String decryptPassword = SecureUtil.md5(presentedPassword);
            // 易数阁前端入为明文密码，资产门户入口为md5加密密码，需要同时判断
            Boolean passwordMatch = SecurityUtils.matchesPassword(presentedPassword, userDetails.getPassword()) || SecurityUtils.matchesPassword(decryptPassword, userDetails.getPassword());
            if (!passwordMatch) {
                this.logger.debug("Authentication failed: password does not match stored value");
                throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }
        }
    }

}
