package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.dataassets.constant.BiViewStateEnum;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsBiViewDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsBiViewMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-25 16:44
 */
@Repository
public class UserDataAssetsBiViewDAOImpl
        extends ServiceImpl<UserDataAssetsBiViewMapper, UserDataAssetsBiView>
        implements UserDataAssetsBiViewDAO {

    @Override
    public List<UserDataAssetsBiView> queryByViewIds(Collection<String> viewIds) {
        if (CollectionUtils.isEmpty(viewIds)) {
            return Collections.emptyList();
        }
        return this.baseMapper.selectList(new QueryWrapper<UserDataAssetsBiView>().lambda()
                .in(UserDataAssetsBiView::getViewId, viewIds));
    }

    @Override
    public Optional<UserDataAssetsBiView> queryByAssetsVersion(String assetsVersion) {
        if (StringUtils.isBlank(assetsVersion)) {
            return Optional.empty();
        }
        List<UserDataAssetsBiView> exist = this.baseMapper.selectList(new QueryWrapper<UserDataAssetsBiView>().lambda()
                .eq(UserDataAssetsBiView::getAssetsVersion, assetsVersion));
        if (CollectionUtils.isEmpty(exist)) {
            return Optional.empty();
        }
        return Optional.ofNullable(exist.get(0));
    }

    @Override
    public List<UserDataAssetsBiView> queryByAssetsVersion(Collection<String> assetsVersion) {
        if (CollectionUtils.isEmpty(assetsVersion)) {
            return Collections.emptyList();
        }
        return this.baseMapper.selectList(new QueryWrapper<UserDataAssetsBiView>().lambda()
                .in(UserDataAssetsBiView::getAssetsVersion, assetsVersion));
    }

    @Override
    public List<UserDataAssetsBiView> queryNeedDeleteView() {
        return this.baseMapper.queryNeedDeleteView();
    }

    @Override
    public void updateDeleteView(Collection<String> viewIds, BiViewStateEnum state) {
        if (CollectionUtils.isEmpty(viewIds)) {
            return;
        }
        this.baseMapper.update(null, new UpdateWrapper<UserDataAssetsBiView>().lambda()
                .set(UserDataAssetsBiView::getDataState, state.name())
                .in(UserDataAssetsBiView::getViewId, viewIds)
        );
    }
}
