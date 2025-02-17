package com.sinohealth.system.biz.ck.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.ck.constant.SnapshotTableHdfsStateEnum;
import com.sinohealth.system.biz.ck.constant.SnapshotTableStateEnum;
import com.sinohealth.system.biz.ck.dao.SnapshotTableMappingDAO;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMapping;
import com.sinohealth.system.biz.ck.mapper.SnapshotTableMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 17:22
 */
@Slf4j
@Repository
public class SnapshotTableMappingDAOImpl
        extends ServiceImpl<SnapshotTableMappingMapper, SnapshotTableMapping>
        implements SnapshotTableMappingDAO {

    @Override
    public SnapshotTableMapping selectByTable(String table) {
        return this.baseMapper.selectOne(new QueryWrapper<SnapshotTableMapping>().lambda().eq(SnapshotTableMapping::getTableName, table));
    }

    @Override
    public List<SnapshotTableMapping> selectByHost(String host) {
        return this.baseMapper.selectList(new QueryWrapper<SnapshotTableMapping>()
                .lambda().eq(SnapshotTableMapping::getHost, host).or().eq(SnapshotTableMapping::getCandidateHost, host));
    }

    @Override
    public void updateHdfsState(Long id, SnapshotTableHdfsStateEnum state) {
        this.update(null, new UpdateWrapper<SnapshotTableMapping>().lambda()
                .set(SnapshotTableMapping::getHdfsState, state.name())
                .eq(SnapshotTableMapping::getId, id));
    }

    @Override
    public void updateState(Long id, SnapshotTableStateEnum state) {
        this.update(null, new UpdateWrapper<SnapshotTableMapping>().lambda()
                .set(SnapshotTableMapping::getState, state.name())
                .eq(SnapshotTableMapping::getId, id));
    }
}
