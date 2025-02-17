package com.sinohealth.framework.web.service;

import com.sinohealth.common.constant.Constants;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.core.redis.RedisCache;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.exception.user.CaptchaException;
import com.sinohealth.common.exception.user.CaptchaExpireException;
import com.sinohealth.common.exception.user.UserPasswordNotMatchException;
import com.sinohealth.common.utils.MessageUtils;
import com.sinohealth.common.utils.phone.MsgCodeUtil;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.framework.security.sms.SmsCodeAuthenticationToken;
import com.sinohealth.system.async.AsyncManager;
import com.sinohealth.system.async.factory.AsyncFactory;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ISysUserTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 登录校验方法
 * 
 *
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysUserTableService userTableService;

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        Date now =new Date();

        // 用户验证
        Authentication authentication = null;
        try
        {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match"),now));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage(),now));
                throw new CustomException(e.getMessage());
            }
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"),now));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        if(StringUtils.isEmpty(loginUser.getUser().getToken())){
            userService.updateLoginTimeToken(UUID.fastUUID().toString(),loginUser.getUserId(),now);
        }else{
            userService.updateLoginTime(loginUser.getUserId(),now);
        }


        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 手机验证码登录验证
     *
     * @param telephone 手机号
     * @param code      验证码
     * @return 结果
     */
    public String loginByTelephone(String telephone, String code) {
        Date now =new Date();

        String verifyKey = Constants.MOBILE_MSG_CODE +"_"+ telephone;
        String captcha = redisCache.getCacheObject(verifyKey);
           if (captcha == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(telephone, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"),now));
            throw new CaptchaExpireException();
        }
        if (!code.equals(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(telephone, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"),now));
            throw new CaptchaException();
        }

        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager
                    .authenticate(new SmsCodeAuthenticationToken(telephone));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(telephone, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match"),now));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(telephone, Constants.LOGIN_FAIL, e.getMessage(),now));
                throw new CustomException(e.getMessage());
            }
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(telephone, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"),now));
        //登录成功去除redis验证码
        MsgCodeUtil.consumeCode(telephone);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 生成token
        return tokenService.createToken(loginUser);
    }


    /**
     * 验证码验证
     *
     * @param keyword 手机号
     * @param code      验证码
     * @return 结果
     */
    public boolean validateCode(String keyword, String code) {
        String verifyKey = Constants.MOBILE_MSG_CODE +"_"+ keyword;
        String captcha = redisCache.getCacheObject(verifyKey);
        MsgCodeUtil.consumeCode(keyword);
        if (captcha == null || !code.equals(captcha)) {
                return  false;
        }
        return true;
    }
}
