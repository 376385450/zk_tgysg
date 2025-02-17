package com.sinohealth.system.domain.converter;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.AuthItemEnum;
import com.sinohealth.common.enums.ResourceType;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.api.cataloguemanageapi.CatalogueDetailDTO;
import com.sinohealth.system.filter.ThreadContextHolder;

/**
 * @Author Rudolph
 * @Date 2023-08-16 11:36
 * @Desc
 */

public class AssetBeanConverter {

    public static void asset2DocInfo(TgAssetInfo tgAssetInfo, CatalogueDetailDTO tgCatalogueBaseInfo, TgDocInfo tgDocInfo) {
        tgDocInfo.setNeed2Audit(true);
        tgDocInfo.setOwnerId(ThreadContextHolder.getSysUser().getUserId());
        if (tgAssetInfo.getIsFollowServiceMenuReadableRange().equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            tgDocInfo.setProcessId(tgCatalogueBaseInfo.getServiceFlowId());
        }
        if (tgAssetInfo.getIsFollowServiceMenuReadableRange().equals(AuthItemEnum.CUSTOM_AUTH)) {
            tgDocInfo.setProcessId(tgAssetInfo.getProcessId());
        }
//        if (tgAssetInfo.getAssetOpenServices().contains(CommonConstants.CAN_DOWNLOAD_PDF_FILE)) {
//            tgDocInfo.setCanDownloadPdf(true);
//        }
//        if (tgAssetInfo.getAssetOpenServices().contains(CommonConstants.CAN_DOWNLOAD_SRC_FILE)) {
//            tgDocInfo.setCanDownloadSourceFile(true);
//        }

    }

    public static void asset2TemplateInfo(TgAssetInfo tgAssetInfo,
                                          CatalogueDetailDTO tgCatalogueBaseInfo,
                                          TgTemplateInfo tgTemplateInfo) {
        tgTemplateInfo.setTemplateName(tgAssetInfo.getAssetName());
        if (tgAssetInfo.getIsFollowServiceMenuReadableRange().equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            tgTemplateInfo.setProcessId(tgCatalogueBaseInfo.getServiceFlowId());
        }
        if (tgAssetInfo.getIsFollowServiceMenuReadableRange().equals(AuthItemEnum.CUSTOM_AUTH)) {
            tgTemplateInfo.setProcessId(tgAssetInfo.getProcessId());
        }
    }

    //    TODO 补充埋点字段,排序字段,负责人字段
    public static TgAssetInfoSimpleDTO assetDocInfo2AssetSimpleDTO(TgAssetInfo a, TgDocInfo tgDocInfo) {
        return new TgAssetInfoSimpleDTO() {{
            setId(a.getId());
            setAssetName(a.getAssetName());
            setType(AssetType.FILE);
            setRelatedId(a.getRelatedId());
            setAssetMenuId(a.getAssetMenuId());
            setMenuName(a.getMenuName());
            setProcessId(a.getProcessId());
            setAssetManagerName(a.getAssetManagerName());
            setFileType(tgDocInfo.getType());
            setShelfState(a.getShelfState());
            setUpdater(a.getUpdater());
            setUpdateTime(a.getUpdateTime());
            setAssetSort(a.getAssetSort());
            setApplyTimes(tgDocInfo.getApplyTimes());
            setApplySucceedTimes(tgDocInfo.getSuccessfulApplyTimes());
            setReadTimes(tgDocInfo.getReadTimes());
            setDownloadPdfTimes(tgDocInfo.getPdfDownloadTimes());
            setDownloadSourceFileTimes(tgDocInfo.getSourceFileDownloadTimes());
        }};
    }

    public static TgAssetInfoSimpleDTO assetTemplateInfo2AssetSimpleDTO(TgAssetInfo a, TgTemplateInfo tgTemplateInfo) {
        return new TgAssetInfoSimpleDTO() {{
            setId(a.getId());
            setAssetName(a.getAssetName());
            setType(AssetType.MODEL);
            setRelatedId(a.getRelatedId());
            setAssetMenuId(a.getAssetMenuId());
            setMenuName(a.getMenuName());
            setProcessId(a.getProcessId());
            setAssetManagerName(a.getAssetManagerName());
            setShelfState(a.getShelfState());
            setUpdater(a.getUpdater());
            setUpdateTime(a.getUpdateTime());
            setAssetSort(a.getAssetSort());
            setRelatedTableName(tgTemplateInfo.getBaseTableName());
        }};
    }

    public static TgAssetInfoSimpleDTO assetTableInfo2AssetSimpleDTO(TgAssetInfo a, TableInfo tableInfo, String providerName) {
        return new TgAssetInfoSimpleDTO() {{
            setId(a.getId());
            setAssetName(a.getAssetName());
            setType(AssetType.TABLE);
            setRelatedId(a.getRelatedId());
            setAssetMenuId(a.getAssetMenuId());
            setProcessId(a.getProcessId());
            setMenuName(a.getMenuName());
            setVersion(tableInfo.getVersion());
            setSyncTime(tableInfo.getSyncTime());
            setAssetManagerName(a.getAssetManagerName());
            setShelfState(a.getShelfState());
            setUpdater(a.getUpdater());
            setUpdateTime(a.getUpdateTime());
            setAssetProvider(a.getAssetProvider());
            setAssetProviderName(providerName);
            setAssetSort(a.getAssetSort());
            setTableName(tableInfo.getTableName());
            setResourceType(ResourceType.TABLE_MANAGEMENT);
        }};
    }

    public static TgAssetInfoSimpleDTO assetMetaDataInfo2AssetSimpleDTO(TgAssetInfo a, TgMetadataInfo tgMetadataInfo, String providerName) {
        return new TgAssetInfoSimpleDTO() {{
            setId(a.getId());
            setAssetName(a.getAssetName());
            setType(AssetType.TABLE);
            setAssetMenuId(a.getAssetMenuId());
            setMenuName(a.getMenuName());
            setAssetManagerName(a.getAssetManagerName());
            setShelfState(a.getShelfState());
            setUpdater(a.getUpdater());
            setUpdateTime(a.getUpdateTime());
            setAssetProvider(a.getAssetProvider());
            setAssetProviderName(providerName);
            setAssetSort(a.getAssetSort());
            setAssetProvider(a.getAssetProvider());
            setAssetProviderName(providerName);
            setTableName(tgMetadataInfo != null ? tgMetadataInfo.getMetaDataTable() : "");
            setResourceType(ResourceType.METADATA_MANAGEMENT);
        }};
    }


}
