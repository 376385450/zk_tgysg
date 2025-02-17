package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.AssetsCataloguePermissionDAO;
import com.sinohealth.system.domain.catalogue.AssetsCataloguePermission;
import com.sinohealth.system.mapper.AssetsCataloguePermissionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@Repository
public class AssetsCataloguePermissionDAOImpl  extends ServiceImpl<AssetsCataloguePermissionMapper, AssetsCataloguePermission> implements AssetsCataloguePermissionDAO {

    @Override
    public List<AssetsCataloguePermission> getAllPermissionsByUserAndDept(Long userId, String deptId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> wrapper = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .eq(AssetsCataloguePermission::getUserId, userId)
                .or()
                .eq(AssetsCataloguePermission::getDeptId, deptId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<AssetsCataloguePermission> getAllPermissionsByCatalogueId(Integer catalogId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> eq = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .eq(AssetsCataloguePermission::getCatalogueId, catalogId);
        return baseMapper.selectList(eq);
    }

    @Override
    public List<AssetsCataloguePermission> getAllPermissionsByCatalogueIds(List<Integer> catalogId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> eq = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .in(AssetsCataloguePermission::getCatalogueId, catalogId);
        return baseMapper.selectList(eq);
    }

    @Override
    public List<AssetsCataloguePermission> getAllPermissionsByCatalogueIdsAndUserIdAndDept(List<Integer> catalogId, Long userId, String deptId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> eq = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .in(AssetsCataloguePermission::getCatalogueId, catalogId)
                .and(
                    wrapper -> {
                        wrapper.or(
                                wrapper_1 -> {
                                    wrapper_1.eq(AssetsCataloguePermission::getUserId, userId);
                                }
                            )
                                .or(
                                wrapper_2 -> {
                                    wrapper_2.eq(AssetsCataloguePermission::getDeptId, deptId);
                                }
                            );
                    }
                );
        return baseMapper.selectList(eq);
    }

    @Override
    public void deleteByCatalogueId(Integer catalogueId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> wrapper = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .eq(AssetsCataloguePermission::getCatalogueId, catalogueId);
        baseMapper.delete(wrapper);
    }


    @Override
    public void saveAll(List<AssetsCataloguePermission> permissions) {
        saveBatch(permissions);
    }

    @Override
    public List<AssetsCataloguePermission> findByUserIdAndDeptId(Long userId, String deptId) {
        final LambdaQueryWrapper<AssetsCataloguePermission> eq = Wrappers.<AssetsCataloguePermission>lambdaQuery()
                .eq(AssetsCataloguePermission::getUserId, userId)
                .or()
                .eq(AssetsCataloguePermission::getDeptId, deptId);
        return baseMapper.selectList(eq);
    }

}
