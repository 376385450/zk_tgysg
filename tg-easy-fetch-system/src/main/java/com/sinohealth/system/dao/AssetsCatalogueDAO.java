package com.sinohealth.system.dao;

import com.sinohealth.system.domain.catalogue.AssetsCatalogue;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/11
 */
public interface AssetsCatalogueDAO {

    List<AssetsCatalogue> selectListInPath(List<Integer> ids);

    List<AssetsCatalogue> selectChild(Integer id);

    void updateAll(List<AssetsCatalogue> catalogues);

    List<AssetsCatalogue> selectListInIds(List<Integer> ids);

    Integer countAllCatalogs();

}
