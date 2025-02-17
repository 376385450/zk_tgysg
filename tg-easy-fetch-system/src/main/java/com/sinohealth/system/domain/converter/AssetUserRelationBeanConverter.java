package com.sinohealth.system.domain.converter;

import com.sinohealth.common.enums.IsCollectEnum;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.TgAssetUserRelation;
import com.sinohealth.system.dto.assets.CollectAssetRequest;
import com.sinohealth.system.dto.assets.ForwardAssetRequest;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 16:32
 */
public class AssetUserRelationBeanConverter {

    public static TgAssetUserRelation toEntity(CollectAssetRequest collectAssetRequest) {
        TgAssetUserRelation tgAssetUserRelation = new TgAssetUserRelation();
        tgAssetUserRelation.setAssetId(collectAssetRequest.getAssetId())
                .setUserId(SecurityUtils.getUserId())
                .setIsCollect(collectAssetRequest.getIsCollect())
                .setCollectTime(new Date())
                .setForwardNum(0)
                .setUpdater(SecurityUtils.getUsername())
                .setUpdateTime(new Date());
        return tgAssetUserRelation;
    }

    public static TgAssetUserRelation toEntity(ForwardAssetRequest forwardAssetRequest) {
        TgAssetUserRelation tgAssetUserRelation = new TgAssetUserRelation();
        tgAssetUserRelation.setAssetId(forwardAssetRequest.getAssetId())
                .setUserId(SecurityUtils.getUserId())
                .setIsCollect(IsCollectEnum.NO.getCode())
                .setCollectTime(null)
                .setForwardNum(1)
                .setUpdater(SecurityUtils.getUsername())
                .setUpdateTime(new Date());
        return tgAssetUserRelation;
    }


    public static TgAssetUserRelation viewAsset(Long assetId) {
        TgAssetUserRelation tgAssetUserRelation = new TgAssetUserRelation();
        tgAssetUserRelation.setAssetId(assetId)
                .setUserId(SecurityUtils.getUserId())
                .setIsCollect(IsCollectEnum.NO.getCode())
                .setCollectTime(null)
                .setForwardNum(0)
                .setViewNum(1)
                .setUpdater(SecurityUtils.getUsername())
                .setUpdateTime(new Date());
        return tgAssetUserRelation;
    }

}
