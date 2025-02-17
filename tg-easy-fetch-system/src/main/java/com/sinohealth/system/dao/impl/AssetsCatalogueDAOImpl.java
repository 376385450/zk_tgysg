package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.AssetsCatalogueDAO;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.mapper.AssetsCatalogueMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/11
 */
@Repository
public class AssetsCatalogueDAOImpl extends ServiceImpl<AssetsCatalogueMapper, AssetsCatalogue> implements AssetsCatalogueDAO {


    @Override
    public List<AssetsCatalogue> selectListInPath(List<Integer> ids) {
        return baseMapper.selectListInPath(ids);
    }

    @Override
    public List<AssetsCatalogue> selectChild(Integer id) {
        final LambdaQueryWrapper<AssetsCatalogue> eq = Wrappers.<AssetsCatalogue>lambdaQuery()
                .eq(AssetsCatalogue::getParentId, id)
                .eq(AssetsCatalogue::getDeleted, 0);
        return baseMapper.selectList(eq);
    }

    @Override
    public void updateAll(List<AssetsCatalogue> catalogues) {
        updateBatchById(catalogues);
    }

    @Override
    public List<AssetsCatalogue> selectListInIds(List<Integer> ids) {
        final LambdaQueryWrapper<AssetsCatalogue> wrapper = Wrappers.<AssetsCatalogue>lambdaQuery()
                .in(AssetsCatalogue::getId, ids)
                .eq(AssetsCatalogue::getDeleted, 0);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Integer countAllCatalogs() {
        final LambdaQueryWrapper<AssetsCatalogue> wq = Wrappers.<AssetsCatalogue>lambdaQuery()
                .eq(AssetsCatalogue::getDeleted, 0);
        return baseMapper.selectCount(wq);
    }

}
