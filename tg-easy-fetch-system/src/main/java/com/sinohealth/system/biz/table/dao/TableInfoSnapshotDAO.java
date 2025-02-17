package com.sinohealth.system.biz.table.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.table.constants.TablePushStatusEnum;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;

import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:21
 */
public interface TableInfoSnapshotDAO extends IService<TableInfoSnapshot> {

    TableInfoSnapshot getLatest(Long tableId);

    TableInfoSnapshot getVersion(Long tableId, Integer version);

    void updatePushStatus(Long id, TablePushStatusEnum status);

    List<TableInfoSnapshot> queryByVersion(Set<String> versionList);
}
