package com.sinohealth.system.biz.ck.constant;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 16:49
 */
public enum SnapshotTableStateEnum {
    /**
     * 创建好主表
     */
    create,
    /**
     * 备份中
     */
    copying,
    /**
     * 备份失败
     */
    copy_fail,
    /**
     * 备份完成
     */
    normal,
    /**
     * 主节点查询异常 故障转移 查询备份节点
     */
    failover

}
