package com.sinohealth.system.domain.value.deliver;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 16:19
 */
@Documented
@Inherited
@Target({TYPE})
@Retention(RUNTIME)
public @interface SupportResourceType {

    DeliverResourceType value();
}
