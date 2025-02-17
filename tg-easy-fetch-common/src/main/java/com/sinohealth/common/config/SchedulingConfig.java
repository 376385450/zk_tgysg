package com.sinohealth.common.config;

import com.sinohealth.common.utils.task.CronTaskRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @program:
 * @description: 定时任务配置类
 * @author: ChenJiaRong
 * @date: 2021/7/30
 **/
@Configuration
public class SchedulingConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        // 定时任务执行线程池核心线程数
        taskScheduler.setPoolSize(4);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadNamePrefix("boot-scheduler-");


        return taskScheduler;
    }

    @Bean
    public CronTaskRegistrar cronTaskRegistrar() {

        return new CronTaskRegistrar();
    }
}
