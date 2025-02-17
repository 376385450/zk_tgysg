package com.sinohealth.framework.config;

import com.sinohealth.common.constant.LogConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.*;

/**
 * 复制业务上下文的 Scheduler 实现类
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-04-02 15:56
 */
@Slf4j
public class BizScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    public BizScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public BizScheduledThreadPoolExecutor(int corePoolSize, String name) {
        this(corePoolSize, new BasicThreadFactory.Builder().namingPattern(name).daemon(true).build());
    }

    public BizScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    /**
     * 手动传递上下文
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        String traceId = MDC.get(LogConstant.TRACE_ID);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return super.schedule(() -> wrapperForRun(command, traceId, auth), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        String traceId = MDC.get(LogConstant.TRACE_ID);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return super.schedule(() -> wrapperForCall(callable, traceId, auth), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        String traceId = MDC.get(LogConstant.TRACE_ID);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return super.scheduleAtFixedRate(() -> wrapperForRun(command, traceId, auth), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        String traceId = MDC.get(LogConstant.TRACE_ID);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return super.scheduleWithFixedDelay(() -> wrapperForRun(command, traceId, auth), initialDelay, delay, unit);
    }

    private static void wrapperForRun(Runnable command, String traceId, Authentication auth) {
        if (StringUtils.isNoneBlank(traceId)) {
            MDC.put(LogConstant.TRACE_ID, traceId);
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            command.run();
        } catch (Throwable e) {
            log.error("", e);
            throw e;
        } finally {
            MDC.remove(LogConstant.TRACE_ID);
        }
    }

    private static <V> V wrapperForCall(Callable<V> command, String traceId, Authentication auth) throws Exception {
        if (StringUtils.isNoneBlank(traceId)) {
            MDC.put(LogConstant.TRACE_ID, traceId);
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            return command.call();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        } finally {
            MDC.remove(LogConstant.TRACE_ID);
        }
    }
}
