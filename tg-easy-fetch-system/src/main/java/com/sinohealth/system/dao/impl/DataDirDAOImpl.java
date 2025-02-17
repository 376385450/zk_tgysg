package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.mapper.DataDirMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-11 10:55
 */
@Repository
public class DataDirDAOImpl extends ServiceImpl<DataDirMapper, DataDir> implements DataDirDAO {
    @Override
    public Map<Long, String> queryParentMap(Collection<Long> dirIds) {
        List<DataDir> dirs = Lambda.queryListIfExist(dirIds, v ->
                baseMapper.selectList(new QueryWrapper<DataDir>().lambda()
                        .select(DataDir::getId, DataDir::getDirName, DataDir::getParentId)
                        .in(DataDir::getId, v)));
        Set<Long> parentIds = dirs.stream().map(DataDir::getParentId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<DataDir> parents = Lambda.queryListIfExist(parentIds, v ->
                baseMapper.selectList(new QueryWrapper<DataDir>().lambda()
                        .select(DataDir::getId, DataDir::getDirName, DataDir::getParentId)
                        .in(DataDir::getId, v)));
        Map<Long, String> parentNameMap = Lambda.buildMap(parents, DataDir::getId, DataDir::getDirName);

        return dirs.stream().collect(Collectors.toMap(DataDir::getId, v -> {
            if (Objects.nonNull(v.getParentId())) {
                return Optional.ofNullable(parentNameMap.get(v.getParentId())).map(p -> p + "-").orElse("") + v.getDirName();
            }
            return v.getDirName();
        }, (front, current) -> current));
    }
}
