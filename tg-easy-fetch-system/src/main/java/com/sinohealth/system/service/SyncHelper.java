package com.sinohealth.system.service;

import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.dto.ApplicationDataDto;

public interface SyncHelper {

    /**
     * 同步任务后置SQL写入，定时检查对应的表是否更新
     */
    void updateApplicationWhichNeed2Update();

//    /**
//     * 系统工具 - 定时任务 - 配置的cron触发
//     */
//    void syncDispatchTask();

//    void autoTransfer(List<Long> ids);

    /**
     * 将内网CK 复杂SQL查询 数据 同步到外网CK 分配客户
     */
    boolean syncApplicationTableToCustomerDatasource(ApplicationDataDto applicationDataDto, UserDataAssets dataAssets);

    boolean syncApplicationTableToCustomerDatasource(ApplicationDataDto applicationDataDto, UserDataAssets dataAssets, Long userId);

    void asyncApplicationTableToCustomerDatasource(Long assetsId, Integer version);

    boolean syncApplicationTableToCustomerDatasource(Long assetsId, Integer version, Long userId);

    /**
     * 将内网资产 推送到内网CK 内网BI
     */
    boolean pushAssetsTableForBI(Long assetsId, Integer version, Long userId);

    /**
     * @see SyncHelper#pushAssetsTableForBI(Long, Integer, Long)
     */
    void asyncPushAssetsTableForBI(Long assetsId, Integer version);

    boolean syncApplicationTableToSelfDatasourceBench(Long assetsId, Long userId);

    boolean syncApplicationTableToSelfDatasourceBench(int i);

    /**
     * 审核通过后，将申请的复杂SQL转换成快照表
     */
    boolean createLocalSnapshotTable(Long applyId, String tableName);

}
