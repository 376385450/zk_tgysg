package com.sinohealth.system.dao;

import com.sinohealth.system.domain.catalogue.AssetsCataloguePermission;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
public interface AssetsCataloguePermissionDAO {

    List<AssetsCataloguePermission> getAllPermissionsByUserAndDept(Long userId, String deptId);

    List<AssetsCataloguePermission> getAllPermissionsByCatalogueId(Integer catalogId);

    List<AssetsCataloguePermission> getAllPermissionsByCatalogueIds(List<Integer> catalogId);

    List<AssetsCataloguePermission> getAllPermissionsByCatalogueIdsAndUserIdAndDept(List<Integer> catalogId, Long userId, String deptId);

    void deleteByCatalogueId(Integer catalogueId);

    void saveAll(List<AssetsCataloguePermission> permissions);

    List<AssetsCataloguePermission> findByUserIdAndDeptId(Long userId, String deptId);


}
