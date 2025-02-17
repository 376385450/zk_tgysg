package com.sinohealth.system.config;

import com.sinohealth.framework.config.ThreadPoolConfig;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-08 22:00
 */
public interface ThreadPoolType {

    /**
     * @see ThreadPoolConfig#syncCkData
     */
    String SYNC_CK = "SYNC_CK";

    /**
     * @see ThreadPoolConfig#postMsg
     */
    String POST_MSG = "POST_MSG";

    String FTP_TASK = "FTP_TASK";

    /**
     * @see ThreadPoolConfig#asyncTask
     */
    String ASYNC_TASK = "ASYNC_TASK";

    /**
     * 实时性要求不高的任务 平缓执行
     */
    String MINI_CK = "MINI_CK";

    /**
     * 常见业务定时执行
     */
    String SCHEDULER = "SCHEDULED_TASK";

    /**
     * CK 有关定时执行
     */
    String SCHEDULER_CK = "SCHEDULED_CK";

    String SCHEDULER_MSG = "SCHEDULED_MSG";

    /**
     * 1. 审核通过后创建快照表 上传FTP
     *
     * @see ThreadPoolConfig#threadPoolTaskExecutor()
     */
    String ENHANCED_TTL = "ENHANCED_TTL";
}
