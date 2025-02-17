package com.sinohealth.system.biz.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-28 11:36
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RedisLock {

    private final RedisTemplate redisTemplate;

    public void wrapperLock(String lockKey, Duration duration, Runnable func) {
        Boolean lock = null;
        try {
            lock = redisTemplate.opsForValue().setIfAbsent(lockKey, 0, duration);
            if (BooleanUtils.isNotTrue(lock)) {
                log.warn("try lock failed {}", lockKey);
                return;
            }

            func.run();

        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (BooleanUtils.isTrue(lock)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    public void wrapperLock(String lockKey, Runnable func) {
        this.wrapperLock(lockKey, Duration.ofMinutes(1), func);
    }

    /**
     * @return 获取锁失败时返回 empty
     */
    public <R> Optional<R> wrapperLock(String lockKey, Callable<R> func) {
        Boolean lock = null;
        try {
            lock = redisTemplate.opsForValue().setIfAbsent(lockKey, 0, Duration.ofMinutes(1));
            if (BooleanUtils.isNotTrue(lock)) {
                log.warn("try lock failed {}", lockKey);
                return Optional.empty();
            }
            return Optional.ofNullable(func.call());
        } catch (Exception e) {
            log.error("", e);
            return Optional.empty();
        } finally {
            if (BooleanUtils.isTrue(lock)) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
