package com.sinohealth.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kuangcp
 * 2024-10-16 19:40
 */
public class CronExpParserUtilTest {
    @Test
    public void testSimple() throws Exception {
        String cn = CronExpParserUtil.translateToChinese("0 0 3/4 ? * TUE,THU *", CronExpParserUtil.CRON_TIME_CN);
        Assert.assertEquals("每周的星期二、每周的星期四、从3点开始,每隔4点执行", cn);
    }
}