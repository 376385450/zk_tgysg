package com.sinohealth.framework.config.ttl;

import com.alibaba.ttl.TtlRunnable;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-06 00:06
 */
public class TtlWrapperExecutor implements Executor {

    private final Executor delegate;

    public TtlWrapperExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(Objects.requireNonNull(TtlRunnable.get(command)));
    }
}
