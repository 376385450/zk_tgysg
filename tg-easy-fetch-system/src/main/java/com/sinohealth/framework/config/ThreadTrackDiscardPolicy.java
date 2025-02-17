package com.sinohealth.framework.config;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-12 15:34
 */
@Slf4j
public class ThreadTrackDiscardPolicy extends ThreadPoolExecutor.DiscardPolicy {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor exe) {
        final int cnt = counter.incrementAndGet();
        log.error("reject {} {}", cnt, exe);
        super.rejectedExecution(r, exe);
    }
}