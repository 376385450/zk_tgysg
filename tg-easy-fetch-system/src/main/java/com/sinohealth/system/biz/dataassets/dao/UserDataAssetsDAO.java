package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.request.MyAssetRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:52
 */
public interface UserDataAssetsDAO extends IService<UserDataAssets> {

    Set<Long> existTemplateId(Collection<Long> templateIds);

    String queryAssetsName(Long id);

    boolean validProjectName(String projectName);

    Map<Long, String> queryAssetsName(Collection<Long> ids);

    /**
     * @return apply -> assetsId
     */
    Map<Long, Long> queryApplyAssets(Collection<Long> applyIds);

    IPage<UserDataAssetResp> queryUserAsset(IPage page, MyAssetRequest myAssetRequest);

    /**
     * @return assetsId -> applyId
     */
    Map<Long, Long> queryAssetsApply(Collection<Long> assetsIds);

    Map<Long, Integer> queryVersion(Collection<Long> assetsIds);

    Integer queryVersion(Long assetsId);

    void fillValid(LambdaQueryChainWrapper<UserDataAssets> wrapper);


    List<UserDataAssets> queryRelateAssetsById(Collection<Long> ids);

    List<UserDataAssets> queryRelateAssets(Collection<Long> tempIds);

    List<UserDataAssets> queryRelateAssets(Long tableId, Integer version,
                                           Boolean skipAssertsBaseVersionFilter, List<String> prodCodes);
}
