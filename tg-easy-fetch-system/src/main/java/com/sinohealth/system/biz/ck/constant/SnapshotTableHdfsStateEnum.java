package com.sinohealth.system.biz.ck.constant;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-14 17:25
 */
public enum SnapshotTableHdfsStateEnum {

    /**
     * 未备份
     */
    none,
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
     * 过期删除
     */
    delete;

    public static final List<String> notNeedBackup = Arrays.asList(normal.name(), delete.name());
}
