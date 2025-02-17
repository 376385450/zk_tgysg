package com.sinohealth.system.domain.asset;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.enums.AssetPermissionType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RelateAssetInfo implements Serializable {


    private String assetName;

    private Long assetId;

    private String assetType;

    private List<AssetPermissionType> permission;

    private Long processId;

    private Long relatedId;

    private String assetBindingDataName;

    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;

    @ApiModelProperty("当前用户对该资产的申请次数")
    private Integer currentUserApplyCount;

}
