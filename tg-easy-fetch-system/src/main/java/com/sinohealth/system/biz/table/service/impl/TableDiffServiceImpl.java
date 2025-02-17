package com.sinohealth.system.biz.table.service.impl;

import com.sinohealth.system.biz.table.service.TableDiffService;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-09 18:45
 */
public class TableDiffServiceImpl implements TableDiffService {

    // TODO 数据同步到HDFS
    public void prepareSyncToHDFS() {
        // 写入两个基本表
        // 创建JOIN大宽表

    }

    /**
     * 读取 大宽表 计算差异
     */
    public void walkAllData() {

        // 读取时只能读取list<map> 无强类型 依据表元信息分辨 指标和维度
    }

    /**
     * 批量写入结果
     */
    private void writeResult() {

    }
}
