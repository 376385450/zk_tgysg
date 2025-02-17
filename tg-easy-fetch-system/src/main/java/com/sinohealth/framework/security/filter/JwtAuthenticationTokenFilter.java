package com.sinohealth.framework.security.filter;

import com.sinohealth.common.constant.Constants;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.ip.AddressUtils;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.framework.web.service.TokenService;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.SysLogininfor;
import com.sinohealth.system.mapper.SysLogininforMapper;
import com.sinohealth.system.service.MockUserService;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * token过滤器 验证token有效性
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLogininforMapper sysLogininforMapper;

    @Autowired
    private MockUserService mockUserService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    @Qualifier(ThreadPoolType.ASYNC_TASK)
    private ThreadPoolTaskExecutor pool;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //保存日志
            asyncSaveLog(loginUser);
        }
        // 调试专用
        if (Objects.isNull(loginUser)) {
            String mockUserId = request.getHeader("mockUserId");
            if (StringUtils.isNotBlank(mockUserId)) {
                mockUserService.fillUserAuthById(request, Long.parseLong(mockUserId));
            }
        }
        chain.doFilter(request, response);
    }

    public void asyncSaveLog(LoginUser user) {

        SysLogininfor sysLogininfor = buildInfo(user.getUsername());

        pool.submit(() -> {
            //今日日期
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            //获取过期时间，在当日12点过期
            long timestamp1 = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long timestamp2 = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long expiredTime = timestamp2 - timestamp1;

            if (expiredTime < 0) {
                return;
            }

            String key = String.format(RedisKeys.SYS_LOGIN_LOCK, today, user.getUserId());
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(key, "1", expiredTime, TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(lock)) {
                try {
                    logger.info("保存登录日志");
                    int insert = sysLogininforMapper.insert(sysLogininfor);
                    if (insert <= 0) {
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    logger.error("保存登录日志异常", e);
                    redisTemplate.delete(key);
                }
            }
        });
    }

    private SysLogininfor buildInfo(String userName) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        String os = userAgent.getOperatingSystem().getName();
        String browser = userAgent.getBrowser().getName();
        SysLogininfor info = new SysLogininfor();
        info.setUserName(userName);
        info.setIpaddr(ip);
        info.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        info.setBrowser(browser);
        info.setOs(os);
        info.setLoginTime(new Date());
        info.setMsg("登录成功");
        info.setStatus(Constants.SUCCESS);
        return info;
    }

}
