package com.sinohealth.system.biz.table.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.table.constants.TablePushStatusEnum;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.mapper.TableInfoSnapshotMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:21
 */
@Repository
public class TableInfoSnapshotDAOImpl
        extends ServiceImpl<TableInfoSnapshotMapper, TableInfoSnapshot>
        implements TableInfoSnapshotDAO {

    @Override
    public TableInfoSnapshot getLatest(Long tableId) {
        return baseMapper.selectOne(new QueryWrapper<TableInfoSnapshot>().lambda()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getLatest, true)
        );
    }

    @Override
    public TableInfoSnapshot getVersion(Long tableId, Integer version) {
        return baseMapper.selectOne(new QueryWrapper<TableInfoSnapshot>().lambda()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getVersion, version)
        );
    }

    @Override
    public void updatePushStatus(Long id, TablePushStatusEnum status) {
        baseMapper.update(null, new UpdateWrapper<TableInfoSnapshot>().lambda()
                .eq(TableInfoSnapshot::getId, id)
                .set(TableInfoSnapshot::getPushStatus, status.name())
        );
    }

    @Override
    public List<TableInfoSnapshot> queryByVersion(Set<String> versionList) {
        if (CollectionUtils.isEmpty(versionList)) {
            return Collections.emptyList();
        }
        return baseMapper.queryByVersion(versionList);
    }
}
