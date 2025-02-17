package com.sinohealth.system.biz.ck.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.ck.constant.SnapshotTableHdfsStateEnum;
import com.sinohealth.system.biz.ck.constant.SnapshotTableStateEnum;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMapping;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 17:22
 */
public interface SnapshotTableMappingDAO extends IService<SnapshotTableMapping> {

    SnapshotTableMapping selectByTable(String table);
    List<SnapshotTableMapping> selectByHost(String host);

    void updateHdfsState(Long id, SnapshotTableHdfsStateEnum state);

    void updateState(Long id, SnapshotTableStateEnum state);

}
