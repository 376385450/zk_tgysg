package com.sinohealth.framework.web.exception;

import com.sinohealth.common.constant.HttpStatus;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.exception.BaseException;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.exception.RateLimitException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalTgExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalTgExceptionHandler.class);

    /**
     * 基础异常
     */
    @ExceptionHandler(BaseException.class)
    public AjaxResult baseException(BaseException e) {
        log.error("", e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public AjaxResult businessException(CustomException e) {
        log.error("", e);
        if (StringUtils.isNull(e.getCode())) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 忽略栈的业务异常
     */
    @ExceptionHandler(RateLimitException.class)
    public AjaxResult rateLimitException(RateLimitException e) {
        return AjaxResult.error(ErrorCode.RATE_LIMIT, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public AjaxResult handlerNoFoundException(Exception e) {
        log.error("", e);
        return AjaxResult.error(HttpStatus.NOT_FOUND, "路径不存在，请检查路径是否正确");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAuthorizationException(AccessDeniedException e) {
        log.error(e.getMessage(), e);
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    @ExceptionHandler(AccountExpiredException.class)
    public AjaxResult handleAccountExpiredException(AccountExpiredException e) {
        log.error(e.getMessage(), e);
        return AjaxResult.error(e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public AjaxResult handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return AjaxResult.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        Long userId = SecurityUtils.getUserIdIgnoreError();
        // 前端漏参数
        if (e instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException mis = (MethodArgumentTypeMismatchException) e;
            log.error("[{}] MIS name:{} val:{} api:{}", userId, mis.getName(), mis.getValue(), mis.getParameter().getExecutable(), e);
        } else {
            log.error(e.getMessage(), e);
        }
        return AjaxResult.error("限制操作,请联系管理员");
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult validatedBindException(BindException e) {
        log.error(e.getMessage());
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object validExceptionHandler(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult illegalArgumentException(IllegalArgumentException e) {
        return AjaxResult.error(400, e.getMessage());
    }
}
