package com.sinohealth.system.domain.constant;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-03-01 16:26
 */
public interface ErrorCode {

    /**
     * 数据同步失败
     */
    int APPLY_DATA_SYNC_FAILED = 10001;

    /**
     * 限流
     */
    int RATE_LIMIT = 10010;

    int REPEAT_FILE = 10020;

    /**
     * 审批 确认框
     */
    int DATA_OVER_LIMIT_CONFIRM = 10021;

    /**
     * 资产升级 确认框
     */
    int TEMPLATE_UPGRADE_CONFIRM = 10022;

}
