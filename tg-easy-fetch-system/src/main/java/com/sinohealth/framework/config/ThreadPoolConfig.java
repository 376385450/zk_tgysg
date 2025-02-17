package com.sinohealth.framework.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.sinohealth.system.biz.hook.processor.ContextCloseHandler;
import com.sinohealth.system.config.ThreadPoolType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @see ContextCloseHandler JVM退出时监控线程池状态
 **/
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 要注意此处的兜底拿不到traceId，因为已经离开线程装饰器的方法被清除了
     * 清理逻辑是合理的，否则线程的traceId标记更加乱套，所以此处的日志只能用于兜底，不能依赖此实现，线程内执行的任务都需要有手动异常捕获
     */
    static Thread.UncaughtExceptionHandler logError;

    static {
        logError = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error("", e);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(logError);
    }


    /**
     * 异步任务执行
     */
    @Bean(name = ThreadPoolType.ASYNC_TASK)
    public ThreadPoolTaskExecutor asyncTask() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(10);
        pool.setKeepAliveSeconds(60);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("async-task-%d").build());
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setQueueCapacity(20);
        return pool;
    }

    /**
     * CK数据同步
     */
    @Bean(name = ThreadPoolType.SYNC_CK)
    public ThreadPoolTaskExecutor syncCkData() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(1);
        pool.setMaxPoolSize(3);
        pool.setKeepAliveSeconds(60);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("sync-ck-%d").build());
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setQueueCapacity(50);
        return pool;
    }

    @Bean(name = ThreadPoolType.POST_MSG)
    public ThreadPoolTaskExecutor postMsg() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(50);
        pool.setKeepAliveSeconds(60);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setAwaitTerminationSeconds(10);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("post-msg-%d").build());
        pool.setRejectedExecutionHandler(new ThreadTrackDiscardPolicy());
        pool.setQueueCapacity(100);
        return pool;
    }

    /**
     * 弹性使用线程 的线程池
     * 为了批量上传FTP使用，需要适度并发
     */
    @Bean(name = ThreadPoolType.FTP_TASK)
    public ThreadPoolTaskExecutor uploadFTP() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(0);
        pool.setMaxPoolSize(10);
        pool.setKeepAliveSeconds(600);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setAwaitTerminationSeconds(10);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("ftp-%d").build());
        pool.setRejectedExecutionHandler(new ThreadTrackDiscardPolicy());
        pool.setQueueCapacity(0);
        return pool;
    }

    @Bean(name = ThreadPoolType.MINI_CK)
    public ThreadPoolTaskExecutor miniCK() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(2);
        pool.setMaxPoolSize(3);
        pool.setAllowCoreThreadTimeOut(true);
        pool.setKeepAliveSeconds(600);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setAwaitTerminationSeconds(10);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("mini-%d").build());
        pool.setRejectedExecutionHandler(new ThreadTrackDiscardPolicy());
        pool.setQueueCapacity(5000);
        return pool;
    }

    /**
     * 注意，为了避免CK连接发生无效的抢用，有可能并发执行ck的sql的地方都需要提交到该线程池 统一做限流
     *
     * <pre>{@code
     *   @Resource
     *   @Qualifier(ThreadPoolType.ENHANCED_TTL)
     *   private Executor ttl;
     * }
     * </pre>
     */
    @Bean(name = ThreadPoolType.ENHANCED_TTL)
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 最大可创建的线程数
        int maxPoolSize = 10;
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(4);
        // 队列最大长度
        int queueCapacity = 5000;
        executor.setQueueCapacity(queueCapacity);
        // 线程池维护线程所允许的空闲时间
        int keepAliveSeconds = 30;
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadTrackDiscardPolicy());
        executor.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("ttl-%d").build());
        executor.setTaskDecorator(new ContextCopyingTaskDecorator());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 执行周期性或定时任务
     * <pre>{@code
     *     @Resource
     *     @Qualifier(ThreadPoolType.SCHEDULER)
     *     private ScheduledExecutorService scheduler;
     * }
     * </pre>
     */
    @Bean(name = ThreadPoolType.SCHEDULER)
    protected ScheduledExecutorService scheduledExecutorService() {
        return new BizScheduledThreadPoolExecutor(10, "schedule-pool-%d");
    }

    /**
     * 定时推送WS 消息
     */
    @Bean(name = ThreadPoolType.SCHEDULER_MSG)
    protected ScheduledExecutorService scheduledMsgService() {
        return new BizScheduledThreadPoolExecutor(2, "ws-msg-%d");
    }

    @Bean(name = ThreadPoolType.SCHEDULER_CK)
    protected ScheduledExecutorService scheduledCk() {
        return new BizScheduledThreadPoolExecutor(2, "ck-%d");
    }
}
