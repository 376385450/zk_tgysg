package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dataassets.constant.BiViewStateEnum;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-25 16:38
 */
public interface UserDataAssetsBiViewDAO extends IService<UserDataAssetsBiView> {

    List<UserDataAssetsBiView> queryByViewIds(Collection<String> viewIds);

    Optional<UserDataAssetsBiView> queryByAssetsVersion(String assetsVersion);

    List<UserDataAssetsBiView> queryByAssetsVersion(Collection<String> assetsVersion);

    List<UserDataAssetsBiView> queryNeedDeleteView();

    void updateDeleteView(Collection<String> viewIds, BiViewStateEnum state);
}
