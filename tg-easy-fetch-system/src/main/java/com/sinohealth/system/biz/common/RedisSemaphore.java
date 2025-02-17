package com.sinohealth.system.biz.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Kuangcp
 * 2024-09-02 09:56
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RedisSemaphore {

    public static final String JUDGE_SCRIPT;

    private final RedisTemplate redisTemplate;

    static {
        JUDGE_SCRIPT = readFile("/lua/semaphore.lua");
    }

    /**
     * @param path 例如 resources 目录下的 redis/lock.lua。传入 /redis/lock.lua
     */
    private static String readFile(String path) {
        try {
            InputStream is = RedisSemaphore.class.getResourceAsStream(path);
            if (Objects.isNull(is)) {
                throw new FileNotFoundException("No file");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s;
            StringBuilder bb = new StringBuilder();
            while ((s = br.readLine()) != null) {
                bb.append(s).append("\n");
            }
            log.info("Load Lua file: {}", path);
            return bb.toString();
        } catch (Exception re) {
            log.error("Load failed {}", path, re);
            return "";
        }
    }

    public void acquireBlock(String key, int permits) throws InterruptedException {
        this.acquireBlock(key, permits, 500);
    }

    public void acquireBlock(String key, int permits, long pollMs) throws InterruptedException {
        try {
            while (!this.acquire(key, permits)) {
                TimeUnit.MILLISECONDS.sleep(pollMs);
            }
        } catch (InterruptedException e) {
            log.error("wait broke key:{}", key, e);
            throw e;
        }
    }

    public boolean acquire(String key, int permits) {
        // 指定 lua 脚本，并且指定返回值类型
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>(JUDGE_SCRIPT, Integer.class);
        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
        Object lockB = redisTemplate.execute(redisScript, Collections.singletonList(key), permits);
        if (Objects.isNull(lockB)) {
            return false;
        }
        return Integer.parseInt(lockB.toString()) > 0;
    }

    public Long release(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public int runCount(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        if (Objects.isNull(val)) {
            return 0;
        }
        return Integer.parseInt(val.toString());
    }

}
