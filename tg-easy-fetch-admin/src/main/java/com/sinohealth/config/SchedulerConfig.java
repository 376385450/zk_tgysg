package com.sinohealth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Kuangcp
 * 2025-01-10 15:06
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
}
