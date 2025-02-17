package com.sinohealth.framework.interceptor;

import com.google.common.util.concurrent.RateLimiter;
import com.sinohealth.common.exception.RateLimitException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-09 16:44
 */
@Slf4j
@Component
public class RateLimiterInterceptor extends HandlerInterceptorAdapter {

    private static final Map<String, RateLimiter> cache = new ConcurrentHashMap<>();

    // TODO nacos 修改配置后，重置已有限流器
    public static final int maxCon = 8;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = SecurityUtils.getUserIdIgnoreError();
        if (Objects.isNull(userId)) {
            return super.preHandle(request, response, handler);
        }
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }
        Method method = ((HandlerMethod) handler).getMethod();
        RateLimit annotation = method.getAnnotation(RateLimit.class);
        boolean needLimit = Objects.nonNull(annotation);
        if (!needLimit) {
            return super.preHandle(request, response, handler);
        }

        RateLimiter limit = cache.computeIfAbsent(userId + ":" + request.getRequestURI(), v -> {
            RateLimiter rateLimiter = RateLimiter.create(maxCon);
            return rateLimiter;
        });

        boolean acquire = limit.tryAcquire();
        if (!acquire) {
            log.warn("user:{} url:{}", userId, request.getRequestURI());
            throw new RateLimitException("请勿频繁操作");
        }

        return super.preHandle(request, response, handler);
    }
}
