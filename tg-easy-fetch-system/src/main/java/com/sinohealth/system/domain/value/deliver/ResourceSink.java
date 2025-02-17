package com.sinohealth.system.domain.value.deliver;


/**
 * 支持链式调用
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 09:08
 */
public interface ResourceSink<S extends ResourceSink<S, R>, R> {

    S setResource(Resource resource);

    Resource getResource();

    S setType(DeliverResourceType type);

    DeliverResourceType getType();

    R process();
}
