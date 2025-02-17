package com.sinohealth.system.biz.transfer.service;

import com.sinohealth.common.core.redis.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-03-18 13:57
 */
@Slf4j
@Service
public class TransferAdapter {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 切换迁移的模式： CMH普通宽表模板、CMH长尾宽表模板
     */
    public String switchTransMode() {
        Object mode = redisTemplate.opsForValue().get(RedisKeys.Apply.TRANSFER_MODE);
        boolean tail = Objects.nonNull(mode) && Objects.equals(mode, "tail");
        if (tail) {
            redisTemplate.opsForValue().set(RedisKeys.Apply.TRANSFER_MODE, "simple");
            return "常规模板";
        } else {
            redisTemplate.opsForValue().set(RedisKeys.Apply.TRANSFER_MODE, "tail");
            return "长尾模板";
        }
    }

    public String switchDebugMode() {
        Object debug = redisTemplate.opsForValue().get(RedisKeys.Apply.DEBUG_MODE);
        if (Objects.nonNull(debug)) {
            redisTemplate.delete(RedisKeys.Apply.DEBUG_MODE);
            return "生产模式";
        } else {
            redisTemplate.opsForValue().set(RedisKeys.Apply.DEBUG_MODE, "1");
            return "Debug模式";
        }
    }

    public String switchFTP() {
        Object debug = redisTemplate.opsForValue().get(RedisKeys.Ftp.BAN_FTP_UPLOAD);
        if (Objects.nonNull(debug)) {
            redisTemplate.delete(RedisKeys.Ftp.BAN_FTP_UPLOAD);
            return "正常FTP上传";
        } else {
            redisTemplate.opsForValue().set(RedisKeys.Ftp.BAN_FTP_UPLOAD, "1");
            return "禁用FTP上传";
        }
    }

    public String stat() {
        Object mode = redisTemplate.opsForValue().get(RedisKeys.Apply.TRANSFER_MODE);
        boolean tail = Objects.nonNull(mode) && Objects.equals(mode, "tail");
        Object debug = redisTemplate.opsForValue().get(RedisKeys.Apply.DEBUG_MODE);
        Object ftp = redisTemplate.opsForValue().get(RedisKeys.Ftp.BAN_FTP_UPLOAD);

        String res = "";
        if (tail) {
            res += "长尾 ";
        } else {
            res += "常规 ";
        }
        if (Objects.nonNull(debug)) {
            res += "调试 ";
        } else {
            res += "提数 ";
        }

        if (Objects.nonNull(ftp)) {
            res += "禁用FTP ";
        } else {
            res += "启用FTP ";
        }
        return res;
    }

}
