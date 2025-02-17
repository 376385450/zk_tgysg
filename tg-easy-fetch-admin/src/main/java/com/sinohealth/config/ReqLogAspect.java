package com.sinohealth.config;

import com.sinohealth.common.annotation.IgnoreBodyLog;
import com.sinohealth.common.annotation.IgnoreLog;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.filter.BodyReaderWrapper;
import com.sinohealth.system.filter.ContextFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller层 业务接口 请求和响应日志
 *
 * @author zhangyanping
 * @date 2023/6/25 10:14
 */
@Component
@Aspect
public class ReqLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ReqLogAspect.class);

    @Autowired
    private AppProperties appProperties;

    @Pointcut("execution(* com.sinohealth.web.controller..*.*(..))")
    public void logWeb() {
    }

    @Pointcut("execution(* com.sinohealth.quartz.controller.SysJobController.*(..))")
    public void logJob() {
    }

    /**
     * @see ContextFilter
     * @see AppProperties#bodyLogPath 打印请求和响应体的路径白名单
     */
    @Around("logWeb()||logJob()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            IgnoreLog ignoreLog = method.getAnnotation(IgnoreLog.class);
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null || Objects.nonNull(ignoreLog)) {
                return joinPoint.proceed();
            }
            HttpServletRequest request = attributes.getRequest();
            String queryString = Optional.ofNullable(request.getQueryString()).map(v -> "?" + v).orElse("");
            String userId = SecurityUtils.getLogUserId();

            boolean needPrint = appProperties.isNeedPrint(request.getRequestURI());

            IgnoreBodyLog ignore = method.getAnnotation(IgnoreBodyLog.class);
            if (needPrint && request instanceof BodyReaderWrapper && Objects.isNull(ignore)) {
                BodyReaderWrapper wrapper = (BodyReaderWrapper) request;
                String bodyStr = new String(wrapper.getRequestBody());
                if (StringUtils.isEmpty(bodyStr)) {
                    logger.info("Req [{}] {}", userId, request.getRequestURI() + queryString);
                } else {
                    logger.info("Req [{}] {} body：{}", userId, request.getRequestURI() + queryString, bodyStr);
                }

                result = joinPoint.proceed();
                long waste = System.currentTimeMillis() - start;

                String rspStr = Optional.ofNullable(result).map(JsonUtils::format).map(v -> {
                    if (v.length() > LogConstant.MAX_RESPONSE_STR_LENGTH) {
                        return v.substring(0, LogConstant.MAX_RESPONSE_STR_LENGTH) + "...";
                    } else {
                        return v;
                    }
                }).orElse(null);
                if (waste > LogConstant.LONG_RT_WARN_MS) {
                    logger.warn("Rsp {}msRT {} {}", waste, request.getRequestURI(), rspStr);
                } else {
                    logger.info("Rsp {}ms {} {}", waste, request.getRequestURI(), rspStr);
                }
            } else {
                logger.info("Req [{}] {} ", userId, request.getRequestURI() + queryString);
                result = joinPoint.proceed();
                long waste = System.currentTimeMillis() - start;
                if (waste > LogConstant.LONG_RT_WARN_MS) {
                    logger.warn("Rsp {}msRT {}", waste, request.getRequestURI());
                }
            }

            return result;
        } catch (Exception e) {
//            logger.error("EXCEPTION ", e);
            throw e;
        }
    }
}
