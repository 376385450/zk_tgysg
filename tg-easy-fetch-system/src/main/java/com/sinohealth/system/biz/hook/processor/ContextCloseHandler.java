package com.sinohealth.system.biz.hook.processor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.config.ThreadPoolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-20 10:40
 */
@Slf4j
@Component
public class ContextCloseHandler implements ApplicationListener<ContextClosedEvent> {

    @Resource
    private List<ThreadPoolTaskExecutor> pools;

    @Autowired
    TaskScheduler bootScheduler;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER_MSG)
    private ScheduledExecutorService msg;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER_CK)
    private ScheduledExecutorService ck;

    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;

    @Autowired
    private AlertService alertService;

    /**
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 暂停所有调度
        if (bootScheduler instanceof ThreadPoolTaskScheduler) {
            ((ThreadPoolTaskScheduler) bootScheduler).shutdown();
        }

        log.warn("event={}", event);
        String hostIp = IpUtils.getHostIp();
        String builder = this.stat();
        String format = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String msg = format + " 应用停止 - " + hostIp + " \n" + builder;
        if (Objects.equals(hostIp, "127.0.0.1")) {
            log.warn("{}", msg);
            return;
        }
        //TODO 等待执行完成？
        alertService.sendDevNormalMsg(msg);
    }

    // 监控Netty 直接内存
//    private AtomicLong nettyDir;
//
//    @PostConstruct
//    public void watchNetty() {
//        Field counter = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
//        counter.setAccessible(true);
//        try {
//            nettyDir = (AtomicLong) counter.get(PlatformDependent.class);
//        } catch (Exception e) {
//
//        }
//
//        scheduler.scheduleAtFixedRate(() -> {
//            log.info("netty direct= {} kib", nettyDir.get() / 1024);
//        }, 10, 5, TimeUnit.SECONDS);
//    }

    public List<TStat> statList() {
        List<TStat> result = new ArrayList<>();
        for (ThreadPoolTaskExecutor pool : pools) {
            result.add(TStat.builder().name(pool.getThreadNamePrefix())
                    .thread(pool.getPoolSize()).run(pool.getActiveCount()).core(pool.getCorePoolSize())
                    .max(pool.getMaxPoolSize()).sum(pool.getThreadPoolExecutor().getTaskCount())
                    .queue(pool.getThreadPoolExecutor().getQueue().size()).build());
        }

        Executor inner = TtlExecutors.unwrap(ttl);
        ThreadPoolTaskExecutor ttlPool = (ThreadPoolTaskExecutor) inner;
        if (Objects.nonNull(ttlPool)) {
            result.add(TStat.builder().name("TTL")
                    .thread(ttlPool.getPoolSize()).run(ttlPool.getActiveCount()).core(ttlPool.getCorePoolSize())
                    .max(ttlPool.getMaxPoolSize()).sum(ttlPool.getThreadPoolExecutor().getTaskCount())
                    .queue(ttlPool.getThreadPoolExecutor().getQueue().size()).build());
        }

        if (scheduler instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor) scheduler;
            result.add(TStat.builder().name("SCHE").thread(pool.getPoolSize()).run(pool.getActiveCount()).core(pool.getCorePoolSize())
                    .sum(pool.getTaskCount()).queue(pool.getQueue().size()).build());
        }
        if (msg instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor) msg;
            result.add(TStat.builder().name("WS").thread(pool.getPoolSize()).run(pool.getActiveCount()).core(pool.getCorePoolSize())
                    .sum(pool.getTaskCount()).queue(pool.getQueue().size()).build());
        }
        if (ck instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor) ck;
            result.add(TStat.builder().name("SCHE-CK").thread(pool.getPoolSize()).run(pool.getActiveCount()).core(pool.getCorePoolSize())
                    .sum(pool.getTaskCount()).queue(pool.getQueue().size()).build());
        }

        return result;
    }

    public String stat() {
        List<TStat> list = statList();
        return list.stream()
                // 忽略Websocket推送线程池
                .filter(v -> !Objects.equals(v.getName(), "WS"))
                .map(TStat::buildRow)
                .collect(Collectors.joining("\n"));
    }
}
