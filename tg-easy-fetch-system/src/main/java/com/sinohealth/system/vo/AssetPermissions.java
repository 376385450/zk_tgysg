package com.sinohealth.system.vo;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/2/23
 */
@Data
public class AssetPermissions implements Serializable {

    @ApiModelProperty("资产id")
    private Long assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产权限控制")
    private List<AssetPermissionType> permission;

    @ApiModelProperty("资产类型")
    private AssetType type;

    @ApiModelProperty("审批流程ID")
    private Long processId;

    @ApiModelProperty("模型id/库表id/文件id")
    private Long relatedId;

    @ApiModelProperty("该用户是否用该资产的可阅读权限")
    private Boolean hasReadPermission;

    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataName;


}
