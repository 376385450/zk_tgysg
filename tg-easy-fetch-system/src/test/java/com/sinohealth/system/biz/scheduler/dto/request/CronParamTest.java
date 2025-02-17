package com.sinohealth.system.biz.scheduler.dto.request;

import org.junit.Test;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-06 19:49
 */
public class CronParamTest {

    @Test
    public void testJson() throws Exception {
        System.out.println(CronParam.newCron("0 0 * * * ? *"));
    }
}
