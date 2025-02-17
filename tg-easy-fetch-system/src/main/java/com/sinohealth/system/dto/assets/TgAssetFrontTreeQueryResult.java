package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.AuthItemEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-25 14:11
 * @Desc
 */
@ApiModel(description = "/api/asset/front_tree_query 返回的数据类")
@Data
public class TgAssetFrontTreeQueryResult implements Serializable {
    @ApiModelProperty("资产ID")
    private Long id;
    @ApiModelProperty("资产绑定数据ID")
    private Long relatedId;
    @ApiModelProperty("资产名称")
    private String assetName = "资产名称";
    @ApiModelProperty("模型(MODEL)/库表(TABLE)/文件(FILE)")
    private AssetType type;
    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataType;
    @ApiModelProperty("资产绑定数据的名称,即资产实体名")
    private String assetBindingDataName;
    @ApiModelProperty("目录ID")
    private Integer assetMenuId;
    @ApiModelProperty("一二级类目名称")
    private String menuName;
    @ApiModelProperty("baseTable")
    private String baseTable;
    @ApiModelProperty("baseTableId")
    private Long baseTableId;
    @ApiModelProperty("资产标签")
    private List<String> assetLabels;
    @ApiModelProperty("资产提供方")
    private String assetProviderId;
    @ApiModelProperty("资产可见范围, 跟随目录(FOLLOW_DIR_AUTH)/自定义(CUSTOM_AUTH)")
    private AuthItemEnum isFollowAssetMenuReadableRange;
    @ApiModelProperty("资产提供方")
    private String assetProvider;
    @ApiModelProperty("资产负责人JSON")
    private String assetManagerJson;
    @ApiModelProperty("资产负责人名")
    private String assetManagerName;
    @ApiModelProperty("资产描述")
    private String assetDescription;
    @ApiModelProperty("更新日期")
    private String updateTime;
    @ApiModelProperty("服务申请次数")
    private Integer serviceCount;
    @ApiModelProperty("浏览次数")
    private Integer viewCount;
    @ApiModelProperty("收藏次数")
    private Integer collectionCount;
    @ApiModelProperty("转发量")
    private Integer shareCount;
    @ApiModelProperty("流程ID")
    private Long processId;
    @ApiModelProperty("资产开放权限")
    private List<AssetPermissionType> permission;
    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;

}
