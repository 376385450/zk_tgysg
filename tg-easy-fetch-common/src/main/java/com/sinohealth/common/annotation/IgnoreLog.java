package com.sinohealth.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 忽略所有日志
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-08-09 15:31
 */
@Documented
@Inherited
@Target({METHOD})
@Retention(RUNTIME)
public @interface IgnoreLog {
}
