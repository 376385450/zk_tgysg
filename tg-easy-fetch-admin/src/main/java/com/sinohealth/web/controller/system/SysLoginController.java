package com.sinohealth.web.controller.system;

import com.sinohealth.common.constant.Constants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysMenu;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginBody;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.core.redis.RedisCache;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.phone.MsgCodeUtil;
import com.sinohealth.common.utils.phone.SmsComponent;
import com.sinohealth.framework.web.service.SysLoginService;
import com.sinohealth.framework.web.service.SysPermissionService;
import com.sinohealth.framework.web.service.TokenService;
import com.sinohealth.system.service.ISysCustomerService;
import com.sinohealth.system.service.ISysMenuService;
import com.sinohealth.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 登录验证
 */
@Api(tags = {"用户登录授权"})
@RestController
public class SysLoginController {
    private Logger logger = LoggerFactory.getLogger(SysLoginApiController.class);

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysCustomerService sysCustomerService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private RedisCache redisCache;


    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        logger.info("----------------login--------------------------");
        AjaxResult ajax = AjaxResult.success();
        //如果是邮箱登录，则查询绑定邮箱的账号
        String username = loginBody.getUsername();
        String regex="^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";
        if(username.matches(regex)) {
           SysUser sysUser =  userService.selectUserByPhoneOrEmail(username);
           if(sysUser == null){
               return AjaxResult.error("该邮箱系统不存在");
           }else {
               username  = sysUser.getUserName();
           }
        }
        // 验证码校验
        String code = redisCache.getCacheObject(Constants.CAPTCHA_CODE_KEY + loginBody.getUuid());
        if (StringUtils.isBlank(code)) {
            return AjaxResult.error("请再次刷新验证码");
        }
        if (!loginBody.getCode().equals(code)) {
            return AjaxResult.error("验证码错误,请重试");
        }
        // 生成令牌
        String token = loginService.login(username, loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        logger.info("----------------login success--------------------------");
        return ajax;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    @ApiOperation("获取用户信息")
    public AjaxResult getInfo() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 云枢组织用户信息
        SinoPassUserDTO orgUserInfo = null;
        if(StringUtils.isNotEmpty(user.getOrgUserId())){
            orgUserInfo = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
        }

        SysCustomer sysCustomer = sysCustomerService.getByUserId(user.getUserId());

        // 权限集合
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        if (null != orgUserInfo) {
            user.setSinoPassUserDTO(orgUserInfo);
            /*ajax.put("orgUserInfo", orgUserInfo);
            ajax.put("userTag", "组织员工");*/
        } else if (null != sysCustomer){
            user.setSysCustomer(sysCustomer);
            /*ajax.put("orgUserInfo", sysCustomer);
            ajax.put("userTag", "客户");*/
        }

        /*ajax.put("tables", userService.getUserTableFromCache(loginUser.getUserId(), true));
        ajax.put("dirIds", userService.getUserDirIdsFromCache(loginUser.getUserId(), true));*/
        ajax.put("permissions", loginUser.getPermissions());

        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    @ApiOperation("获取路由信息")
    public AjaxResult getRouters() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        // 用户信息
        SysUser user = loginUser.getUser();
        Long userId = user.getUserId();
        if(user.getIsSubAccount() !=null && user.getIsSubAccount() == 1){
            userId = user.getParentAccountId();
        }
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus,true));
       /* RouterDto dto = new RouterDto();
        dto.setMenusRouter(menuService.buildMenus(menus, false));
        dto.setVueRouter(menuService.buildMenus(menus, true));

        return AjaxResult.success(dto);*/
    }

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/loginByTelephone")
    @ApiOperation("手机号登录")
    public AjaxResult loginByTelephone(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.loginByTelephone(loginBody.getTelephone(), loginBody.getCode());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }


    @GetMapping("/captchaNum")
    @ApiOperation("获取验证码")
    public AjaxResult capt(@RequestParam("keyword") String keyword) {
        String phoneRex = "^((13[0-9])|(14[0|5|6|7|9])|(15[0-3])|(15[5-9])|(16[6|7])|(17[2|3|5|6|7|8])|(18[0-9])|(19[1|8|9]))\\d{8}$";
        String emailRex  = "^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";
        int type = 0;
        //手机号、邮箱正则判断，1：手机；2邮箱
        if(keyword.matches(phoneRex)){
            type = 1;
        }else if(keyword.matches(emailRex)){
            type = 2;
        }
        AjaxResult ajaxResult  = null;
        SysUser sysUser = userService.selectUserByPhoneOrEmail(keyword);

        if(sysUser == null){
            ajaxResult = AjaxResult.error("该手机号/邮箱系统未绑定用户");
        }else{
            try {
                if( type == 1){
                    MsgCodeUtil.sendCode(sysUser.getRealName(),keyword);
                }else if(type == 2){
                    MsgCodeUtil.sendEmailCode(sysUser.getRealName(),keyword);
                }
                ajaxResult = AjaxResult.success();
            } catch (CustomException e) {
                ajaxResult =  AjaxResult.error(e.getMessage());
            }catch (Exception e) {
                ajaxResult =  AjaxResult.error(e.getMessage());
            }
        }

        return ajaxResult;
    }
    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/validateCode")
    @ApiOperation("验证码验证")
    public AjaxResult validateCode(@RequestParam("code") String code,@RequestParam("keyword") String keyword) {
       if(loginService.validateCode(keyword,code)){
            return AjaxResult.success();
       }else {
           return  AjaxResult.error("验证码不正确");
       }
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/phoneIsExit")
    @ApiOperation("手机号是否存在校验")
    public AjaxResult phoneIsExit(@RequestParam("phoneNum") String phoneNum) {
        if(userService.selectCntByPhone(phoneNum) == 0){
            return AjaxResult.success();
        }else {
            return  AjaxResult.error("手机号已存在");
        }
    }

    @GetMapping("/fristCaptchaNum")
    @ApiOperation("首次绑定获取手机验证码")
    public AjaxResult capt2(@RequestParam("phoneNum") String phoneNum,@RequestParam("name") String name) {
        AjaxResult ajaxResult  = null;
            try {
                MsgCodeUtil.sendCode(name,phoneNum);
                ajaxResult = AjaxResult.success();
            } catch (CustomException e) {
                ajaxResult =  AjaxResult.error(e.getMessage());
            }catch (Exception e) {
                ajaxResult =  AjaxResult.error(e.getMessage());
            }
        return ajaxResult;
    }

}
