package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.WhitlistServiceType;
import com.sinohealth.system.biz.assets.dto.TableAssetsListVO;
import com.sinohealth.system.biz.dataassets.dto.AssetValidateDTO;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetStaffParam;
import com.sinohealth.system.domain.asset.RelateAssetInfo;
import com.sinohealth.system.domain.asset.TgAssetRelateResp;
import com.sinohealth.system.dto.GuideDTO;
import com.sinohealth.system.dto.assets.*;
import com.sinohealth.system.vo.CollectListVo;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IAssetService {

    void addAsset(TgAssetInfo assetInfo);

    void removeAssetRelateWhenChangePath(TgAssetInfo assetInfo);

    void addAssetRelate(TgAssetInfo assetInfo);

    void deleteAllAssetRelated(TgAssetInfo assetInfo);

    AjaxResult getAllAsset();

    AjaxResult<TgAssetRelateResp> relatableAsset(Integer assetId, Integer catalogId, Integer lastCatalogId);

    AjaxResult<AssetValidateDTO> validateSort(Long assetId, Integer sortNum);

    AjaxResult<Boolean> changeSort(ChangeSortDTO changeSortDTO);

    Long getMaxValue(AssetType assetType);

    void addAssetAuthInfo(TgAssetInfo assetInfo);

    GuideDTO guide(Long assetId);

    void insertAssetAuth(TgAssetInfo assetInfo, TgAssetStaffParam i, WhitlistServiceType type);

    List<TgAssetInfo> queryList(List<Long> relatedIds, AssetType type);

    Page<TgAssetInfo> queryPage(AssetBackendPageQuery queryParam);

    TgAssetInfo queryOne(Long relatedId, AssetType type);

    TgAssetInfo queryOne(Long id);

    void delete(Long relatedId, AssetType type);

    AjaxResult<?> delete(Long id);

    AjaxResult<?> query(@ModelAttribute AssetBackendPageQuery queryParam);

    List<RelateAssetInfo> buildRelate(Long id);

    AjaxResult<?> frontTreeQuery(AssetFrontendPageQuery queryParam);

    List<TgAssetFrontTreeQueryResult> allReadableAsset();

    /**
     * 计算用户对某一资产拥有的权限
     * @param assetId 资产id
     * @param userId 用户id
     * @param deptId 部门id
     * @param isIncludeApplication 是否包含申请的数据权限, false 只计算资产负责人和白名单的权限, true 包含申请的权限
     * @return
     */
    List<AssetPermissionType> computePermissions(TgAssetInfo tgAssetInfo, Long userId, String deptId, boolean isIncludeApplication);

    /**
     * 批量
     * @param tgAssetInfoList
     * @param userId
     * @param deptId
     * @param isIncludeApplication
     * @return
     */
    Map<TgAssetInfo, List<AssetPermissionType>> computePermissions(List<TgAssetInfo> tgAssetInfoList, Long userId, String deptId, boolean isIncludeApplication);

    /**
     * 收藏资产
     *
     * @param collectAssetRequest
     * @return
     */
    AjaxResult<Object> collectAsset(CollectAssetRequest collectAssetRequest);

    /**
     * 转发资产
     *
     * @param forwardAssetRequest
     * @return
     */
    AjaxResult<Object> forwardAsset(ForwardAssetRequest forwardAssetRequest);


    AjaxResult<Object> viewAsset(Long assetId);

    /**
     * 收藏列表
     *
     * @param collectListRequest
     * @return
     */
    AjaxResult<PageInfo<CollectListVo>> collectList(CollectListRequest collectListRequest);

    int checkSameAssetName(Long id, String assetName);

    AjaxResult<Object> judgeViewable(JudgeViewableRequest judgeViewableRequest);

    /**
     * 填充审批流id
     */
    AjaxResult<?> fillProcessId4FollowMenuDirItem(TgAssetInfo tgAssetInfo);

    AjaxResult<?> myApplicationQuery(AssetApplicationPageQuery queryParam);

    AjaxResult<?> myApplicationCount();

    List<TgAssetInfo> queryAll();

    Integer computeCurrentUserApplyCount(Long assetId, Long userId);

    Map<Long, Integer> computeCurrentUserApplyCount(Long userId, Collection<Long> assetIds);

    AjaxResult<?> lastAssetQuery(LastAssetQuery queryParam);

    AjaxResult<?> assetIndicatorQuery(AssetIndicatorQuery queryParam);

    AjaxResult<List<TableAssetsListVO>> queryFlowTable();
}
