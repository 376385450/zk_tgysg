package com.sinohealth.system.biz.dataassets.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AuditTypeEnum;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.system.biz.application.dto.request.ApplicationSaveAsRequest;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.ProcessDefStatusDTO;
import com.sinohealth.system.biz.dataassets.dto.UpsertAssetsBO;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsSyncDTO;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsVersionPageDTO;
import com.sinohealth.system.biz.dataassets.dto.bo.ExecFlowParam;
import com.sinohealth.system.biz.dataassets.dto.request.*;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.domain.TgNoticeRead;
import com.sinohealth.system.domain.TgTemplateInfo;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:56
 */
public interface UserDataAssetsService {

    /**
     * 创建或更新 数据资产
     * <p>
     * 上游：审核通过，尚书台回调
     */
    void upsertDataAssets(UpsertAssetsBO bo);

    /**
     * 另存资产
     *
     * @return 复制出来的资产id
     */
    Long saveAs(ApplicationSaveAsRequest request);

    AjaxResult<Void> createDataAssetsByCallback(UserAssetsCallbackRequest request);

    /**
     * 我的数据 V1
     */
//    List<Node> queryUserAssets(DataDirRequest request);

    AjaxResult<PageInfo<UserDataAssetResp>> listMyAsset(MyAssetRequest request);

    /**
     * 我的数据 V2
     */
//    List<Node> queryUserAssetsByTree(AssetsDirRequest request);

    /**
     * 我的数据 V1.9.0 V3
     */
    List<AssetsNode> queryUserAssetsTree(AssetsDirRequest request);

    /**
     * V1.9.0 列表API
     */
    List<AssetsNode> queryUserAssets(AssetsDirRequest request);

    List<UserDataAssets> queryAllValidAssetsByUserId(boolean expire, boolean sync);

    AjaxResult<List<String>> assetsTimeGra(AssetsDirRequest request);

    AjaxResult<List<UserDataAssetsSyncDTO>> querySyncList();

    boolean getNeedUpdate(Long applyId, String tableName);

    /**
     * @param applicationId 申请id
     */
    AjaxResult executeWorkFlow(Long applicationId);

    /**
     * 混合工作流执行，文件交付
     */
    AjaxResult mixExecuteForApply(Long applicationId);

    /**
     * 调用申请绑定的尚书台工作流
     *
     * @param applicationId 申请id
     * @param triggerId     触发配置id（标记手动还是自动）
     */
    AjaxResult executeWorkFlow(ExecFlowParam param);

    void dolphinCallBack(String instanceUid, Integer state);

    AjaxResult<ProcessDefStatusDTO> queryWorkflowStatus(Long applicationId);

    /**
     * 分页查询历史版本
     */
    AjaxResult<IPage<UserDataAssetsVersionPageDTO>> pageQueryAssetsSnapshot(UserDataAssetsVersionPageRequest request);

    AjaxResult<Void> editAssetsInfo(UserDataAssetsVersionEditRequest request);


    void replaceAssets(UpsertAssetsBO bo, Long assetsId, TgTemplateInfo template);

    void syncApplicationNo(Long id);

    boolean deleteSaveAs(Long id);


    /**
     * 异步更新数量和 prodcode 信息
     */
    void asyncUpdateCount(UserDataAssets assets);

    void copyNewNotice(List<TgNoticeRead> tgNoticeReads, Integer version, AuditTypeEnum type);

    AjaxResult<Void> manualDeprecated(Long assetsId, Long applyId);

    AjaxResult<Void> manualDeprecatedByApply(Long applyId);

    List<FlowAssetsPageDTO> listForCreate(FlowAssetsPageRequest request);

    List<FlowAssetsPageDTO> listForCreate(FlowAssetsAutoPageRequest request);

    void handleReplaceSaveAsAssets(String tableName, Long mainId, Integer version);
}
