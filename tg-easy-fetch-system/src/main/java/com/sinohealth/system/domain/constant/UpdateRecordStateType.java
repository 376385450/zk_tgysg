package com.sinohealth.system.domain.constant;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-10 17:13
 */
public interface UpdateRecordStateType {

    /**
     * 未同步过，即同步表不存在对应申请的同步记录
     */
    int NONE = 0;
    /**
     * 提交进线程池，等待执行
     */
    int WAIT_UPDATE = 1;
    /**
     * 推送数据中
     */
    int UPDATING = 2;

    /**
     * 结束态 成功
     */
    int SUCCESS = 3;
    /**
     * 结束态 失败
     */
    int FAILED = 4;
}
