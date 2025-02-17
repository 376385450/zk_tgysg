package com.sinohealth.system.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.dto.api.cataloguemanageapi.*;

import java.util.List;
import java.util.Map;

/**
 * @Description 数据资产类目管理
 * @Author shallwetalk
 * @Date 2023/8/10
 */
public interface DataAssetsCatalogueService {

    CatalogueDetailDTO getCatalogueBaseInfo(Integer id);

    List<CatalogueAllDTO> getCatalogueWithoutPermission();

    List<UserPermissionDTO> getInheritedPermissions(Integer parentId);

    List<CatalogueQueryDTO> getCatalogueTree();

    CatalogueDataReadTree getReadAbleCatalogue();

    List<CatalogueAssetManageAbleDTO> getAssetsManageAbleCatalogue();

    Page<UserDTO> getSelectedUser(Integer pageNum, Integer pageSize, String name);

    List<DeptDTO> getSelectedDept();

    Integer saveOrUpdate(CatalogueDTO catalogueDTO);

    void deleteCatalogue(Integer id);

    /**
     * 判断两个目录是否在同一个顶级目录下
     * @param catalogId
     * @param catalogId2
     * @return
     */
    boolean isSameTopCatalog(Integer catalogId, Integer catalogId2);


    /**
     * 资产目录中文转换
     *
     * @param path
     * @return
     */
    String getCataloguePathCn(String path);

    Integer getLevel1CatalogueId(Integer catalogueId);

    Map<Integer, String> getLevel12MenuNames();

    Map<Integer, String> getFullMenuNames();

    String buildCatalogFullPath(String path, Map<Integer, AssetsCatalogue> catalogueIdNameMap);

    /**
     * 资产是否可读性
     *
     * @return true 可读， false 不可读
     */
    boolean assetReadAble(String type, Long relatedId);

    List<Integer> getManageableAssetMenuIds(Integer dirId);

    List<AssetsCatalogue> getMenuIdsByMenuRootIds(List<Integer> menuId);

    AssetsCatalogue getInitialCatalogue();

    AssetsCatalogue getCatalogueByDirName(String dirName);
}
