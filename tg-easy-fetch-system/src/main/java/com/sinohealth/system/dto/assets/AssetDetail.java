package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.system.domain.AssetLink;
import com.sinohealth.system.domain.asset.RelateAssetInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@ApiModel("资产详情")
@Data
public class AssetDetail implements Serializable {

    @ApiModelProperty("是否有阅读权限")
    private Boolean readable;

    @ApiModelProperty("审批流程id")
    private Long processId;

    @ApiModelProperty("relatedId")
    private Long relatedId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产类目")
    private String assetMenu;

    private String baseTable;

    @ApiModelProperty("资产提供方")
    private String assetProvider;

    @ApiModelProperty("当前用户是否是资产负责人")
    private boolean currentUserManageAsset;

    @ApiModelProperty("当前用户是否有资产管理权限")
    private boolean currentUserAssetManagerPermissions;

    @ApiModelProperty("资产负责人")
    private String assetManagerName;

    @ApiModelProperty("资产标签")
    private List<String> assetLabel;

    @ApiModelProperty("资产描述")
    private String assetDescription;

    @ApiModelProperty("使用说明")
    private String assetUsage;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @ApiModelProperty("资产分类")
    private String assetType;

    @ApiModelProperty("数据库类型")
    private String dbType;

    @ApiModelProperty("挂接资源")
    private String assetHang;

    @ApiModelProperty("文档类型")
    private String assetFileType;

    @ApiModelProperty("是否元数据类型")
    private boolean isMetadataType;

    @ApiModelProperty("metadataId")
    private Integer metadataId;

    @ApiModelProperty("模型名称")
    private String moduleName;

    @ApiModelProperty("资源库表")
    private String assetTable;

    @ApiModelProperty("表id(table_info字段)")
    private Long tableId;

    @ApiModelProperty("所属业务线")
    private String owningBusinessLine;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("收藏量")
    private Integer collectionCount;

    @ApiModelProperty("转发量")
    private Integer shareCount;

    @ApiModelProperty("服务申请次数")
    private Integer serviceCount;

    @ApiModelProperty("资产权限控制")
    private List<AssetPermissionType> permission;

    @ApiModelProperty("资产关联")
    private List<RelateAssetInfo> tgAssetRelate;

    @ApiModelProperty("资产关联链接")
    private List<AssetLink> assetLinks;

    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;


}
