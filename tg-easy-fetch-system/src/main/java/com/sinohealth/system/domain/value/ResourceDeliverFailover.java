package com.sinohealth.system.domain.value;

import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;

import java.util.function.Predicate;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-22 17:12
 */
public class ResourceDeliverFailover {

    /**
     * 失败切换
     * @param strategy
     * @param failoverStrategy
     * @param dataSource
     * @param predicate
     * @return
     * @param <T>
     * @param <R1>
     * @param <R2>
     * @throws Exception
     */
    public static <T extends DataSource, R1 extends Resource, R2 extends Resource> Resource
    deliver(ResourceDeliverStrategy<T, R1> strategy, ResourceDeliverStrategy<T, R2> failoverStrategy, T dataSource,
            Predicate<Exception> predicate) throws Exception {
        try {
            return strategy.deliver(dataSource);
        } catch (Exception e) {
            if (predicate.test(e)) {
                return failoverStrategy.deliver(dataSource);
            } else {
                throw e;
            }
        }
    }
}
