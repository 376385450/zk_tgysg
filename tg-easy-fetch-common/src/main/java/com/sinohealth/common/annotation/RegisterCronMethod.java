package com.sinohealth.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 只是标识哪些方法被注册到了框架里成为定时任务，无代码效果
 * <p>
 * <p>
 * 系统工具>定时任务 中调度执行，通过IOC得到Bean后反射执行
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 19:10
 */
@Target({ElementType.METHOD})
public @interface RegisterCronMethod {
}
