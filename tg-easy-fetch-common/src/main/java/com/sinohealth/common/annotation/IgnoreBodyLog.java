package com.sinohealth.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 如果方法使用了该注解则，只记录请求url和耗时，不记录请求和响应体
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-08-09 14:27
 */
@Documented
@Inherited
@Target({METHOD})
@Retention(RUNTIME)
public @interface IgnoreBodyLog {
}
