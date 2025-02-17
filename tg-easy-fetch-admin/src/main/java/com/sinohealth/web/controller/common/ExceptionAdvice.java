package com.sinohealth.web.controller.common;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.exception.ApplicationValidateException;
import com.sinohealth.common.exception.TemplateValidateException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author Rudolph
 * @Date 2022-08-18 9:39
 * @Desc
 */
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler({ApplicationValidateException.class})
    public AjaxResult ApplicationValidateExceptionHandler(ApplicationValidateException e) {
        return AjaxResult.error("执行配置过程中出现错误,请确认申请配置无误");
    }
    @ExceptionHandler({TemplateValidateException.class})
    public AjaxResult TemplateValidateExceptionHandler(TemplateValidateException e) {
        return AjaxResult.error("执行配置过程中出现错误,请确认模板配置无误");
    }
}
