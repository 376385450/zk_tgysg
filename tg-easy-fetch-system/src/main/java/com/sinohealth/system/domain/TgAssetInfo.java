package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.AuthItemEnum;
import com.sinohealth.common.enums.ResourceType;
import com.sinohealth.common.utils.SpringContextUtils;
import com.sinohealth.system.domain.asset.RelateAssetInfo;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.mapper.AssetsCatalogueMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-07 11:14
 * @Desc
 */

@ApiModel(description = "资产信息表(tg_asset_info)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_asset_info")
@EqualsAndHashCode(callSuper = false)
public class TgAssetInfo extends Model<TgAssetInfo> {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty("模型id/库表id/文件id")
    private Long relatedId;
    @ApiModelProperty("元数据ID")
    private Long metaId;
    @ApiModelProperty("模版(MODEL)/库表(TABLE)/文件(FILE)")
    private AssetType type;
    @ApiModelProperty("资产名称")
    @Size(max = 100, message = "名称长度超出限制")
    private String assetName;
    @ApiModelProperty("资产编码")
    private String assetCode;
    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataType;
    @ApiModelProperty("资产绑定数据的名称,即资产实体名")
    private String assetBindingDataName;
    @ApiModelProperty("资产类目")
    private Integer assetMenuId;
    @ApiModelProperty("一二级类目名称")
    @TableField(exist = false)
    private String menuName;
    @ApiModelProperty("资产标签")
    @TableField(exist = false)
    private List<Integer> assetLabels;
    @ApiModelProperty("资产标签JSON")
    @JsonIgnore
    private String assetLabelsJson;
    @ApiModelProperty("新增的资产标签")
    @TableField(exist = false)
    private List<String> newTag;
    @ApiModelProperty("资产提供方")
    private String assetProvider;

    @ApiModelProperty("资产负责人")
    @TableField(exist = false)
    private List<String> assetManager;

    @ApiModelProperty("资产负责人名")
    @TableField(exist = false)
    private String assetManagerName;

    @ApiModelProperty("资产负责人JSON")
    @JsonIgnore
    private String assetManagerJson;

    @ApiModelProperty("上架状态：未上架(默认)、已上架、已下架")
    private String shelfState;
    @ApiModelProperty("资产描述")
    private String assetDescription;
    @ApiModelProperty("使用说明")
    private String assetUsage;

    @ApiModelProperty("资产可见范围, 跟随目录(FOLLOW_DIR_AUTH)/自定义(CUSTOM_AUTH)")
    private AuthItemEnum isFollowAssetMenuReadableRange;
    @ApiModelProperty("资产可见范围")
    @TableField(exist = false)
    private List<TgAssetStaffParam> customAssetReadableWhiteList;

    @ApiModelProperty("资产可见范围JSON")
    @JsonIgnore
    private String customAssetReadableWhitelistJson;

    @ApiModelProperty("资产开放服务")
    @TableField(exist = false)
    private List<AssetPermissionType> assetOpenServices;

    @ApiModelProperty("资产开放服务JSON")
    @JsonIgnore
    private String assetOpenServicesJson;

    @ApiModelProperty("无需审核服务")
    @TableField(exist = false)
    private List<AssetPermissionType> nonAuditAssetOpenServices;

    @ApiModelProperty("无需审核服务JSON")
    @JsonIgnore
    private String nonAuditAssetOpenServicesJson;

    @ApiModelProperty("查询开发条数")
    private Integer queryLimit;

    @ApiModelProperty("服务申请流程, 跟随目录(FOLLOW_DIR_AUTH)/自定义(CUSTOM_AUTH)")
    private AuthItemEnum isFollowServiceMenuReadableRange;

    @ApiModelProperty("审批流程ID")
    private Long processId;

    @ApiModelProperty("服务白名单")
    @TableField(exist = false)
    private List<TgAssetStaffParam> serviceWhiteList;

    @ApiModelProperty("资产关联")
    @TableField(exist = false)
    private List<RelateAssetInfo> assetInfos;

    @ApiModelProperty("资产关联链接")
    @TableField(exist = false)
    private List<AssetLink> assetLinks;

    @ApiModelProperty("是否开启操作指引 0否 1是")
    private Integer guide;

    @ApiModelProperty("指引说明")
    private String guideDesc;

    @ApiModelProperty("操作指引关联链接")
    @TableField(exist = false)
    private List<AssetLink> guideLinks;

    @ApiModelProperty("服务白名单JSON")
    @JsonIgnore
    private String serviceWhitelistJson;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @ApiModelProperty("排序")
    private Long assetSort;

    @TableLogic
    @ApiModelProperty("是否删除")
    private Long deleted;

    @ApiModelProperty("资源类型： 表管理(TABLE_MANAGEMENT), 元数据(METADATA_MANAGEMENT)")
    private ResourceType resourceType;

    @TableField(exist = false)
    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;

    @TableField(exist = false)
    @ApiModelProperty("当前用户对该资产的申请次数")
    private Integer currentUserApplyCount;


    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgAssetInfo newInstance() {
        return new TgAssetInfo();
    }

    public Long getProcessId() {
        if (this.isFollowServiceMenuReadableRange.equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            // 返回目录的流程ID
            AssetsCatalogueMapper catalogueMapper = SpringContextUtils.getBean(AssetsCatalogueMapper.class);
            AssetsCatalogue assetsCatalogue = catalogueMapper.selectById(this.assetMenuId);
            return assetsCatalogue.getServiceFlowId();
        }
        // 返回本身的流程ID
        return processId;
    }
}


