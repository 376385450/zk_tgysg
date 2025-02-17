package com.sinohealth.common.utils.uuid;

import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ID生成器工具类
 * 
 *
 */
public class IdUtils
{
    private static Lock lock = new ReentrantLock();

    /**
     * 获取随机UUID
     * 
     * @return 随机UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 随机UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
    }


    public static String generateBusinessSerialNo() {
        try {
            lock.lock();
            StringBuffer str = new StringBuffer();
            for (int i = (int) 'A'; i <= (int) 'Z'; i++) {
                str.append((char) i);
            }
            char[] zm = str.toString().toCharArray();
            int zz = zm.length;
            StringBuffer newStr = new StringBuffer();
            String time = DateUtils.dateTimeNow("yyyyMMddHHmmss");
            newStr.append(zm[Integer.parseInt(time.substring(0, 4)) % zz]);
            newStr.append(zm[Integer.parseInt(time.substring(6, 8)) % zz]);
            newStr.append(zm[Integer.parseInt(time.substring(10, 12)) % zz]);
            newStr.append(zm[Integer.parseInt(time.substring(12, 14)) % zz]);
            newStr.append(DateUtils.dateTimeNow(time));
            Random random = new Random();
            for (int i = 0; i < (32 - newStr.length()); i++) {
                newStr.append(random.nextInt(10));
            }
            return newStr.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        } finally {
            lock.unlock();
        }
    }

    public static long getUUID(String... fromString) {
        String join = StringUtils.join(fromString, ",");
        long result = java.util.UUID.nameUUIDFromBytes(join.getBytes(StandardCharsets.UTF_8)).getMostSignificantBits() / 10000;
        return result > 0 ? result : result * (-1);
    }
}
