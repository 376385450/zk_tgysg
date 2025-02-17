package com.sinohealth.common.utils;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-02 11:50
 */
@FunctionalInterface
public interface Call<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     */
    V call();
}