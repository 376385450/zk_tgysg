package com.sinohealth.system.biz.dataassets.dao.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:53
 */
@Slf4j
@Repository
public class UserDataAssetsSnapshotDAOImpl
        extends ServiceImpl<UserDataAssetsSnapshotMapper, UserDataAssetsSnapshot>
        implements UserDataAssetsSnapshotDAO {

    @Override
    public int countSnapshot(Long assetsId) {
        Integer count = baseMapper.selectCount(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                .eq(UserDataAssetsSnapshot::getAssetsId, assetsId));
        return Optional.ofNullable(count).orElse(0);
    }

    @Override
    public Map<Long, Integer> batchCountSnapshot(Collection<Long> assetsId) {
        if (CollectionUtils.isEmpty(assetsId)) {
            return Collections.emptyMap();
        }

        List<UserDataAssetsSnapshot> list = baseMapper.groupByAssetsId(assetsId);

        return Lambda.buildMap(list, UserDataAssetsSnapshot::getAssetsId, UserDataAssetsSnapshot::getVersion);
    }

    @Override
    public UserDataAssetsSnapshot queryByAssetsId(Long assetsId, Integer version) {
        if (Objects.isNull(assetsId) || Objects.isNull(version)) {
            log.warn("参数缺失 id={} version={}", assetsId, version);
            return null;
        }

        List<UserDataAssetsSnapshot> list = baseMapper.selectList(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                .eq(UserDataAssetsSnapshot::getAssetsId, assetsId).eq(UserDataAssetsSnapshot::getVersion, version)
        );
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<UserDataAssetsSnapshot> queryByAssetsIds(Collection<Long> assetsIds) {
        return baseMapper.selectList(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                .in(UserDataAssetsSnapshot::getAssetsId, assetsIds)
        );
    }
}
