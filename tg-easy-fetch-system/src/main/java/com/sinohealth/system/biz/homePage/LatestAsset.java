package com.sinohealth.system.biz.homePage;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@ApiModel("最新资产")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LatestAsset implements Serializable {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("资产名称")
    private String name;

    @ApiModelProperty("发布时间")
    private String time;

    @ApiModelProperty("资产权限控制")
    private List<AssetPermissionType> permission;

    @ApiModelProperty("资产类型")
    private AssetType type;

    @ApiModelProperty("审批流程ID")
    private Long processId;

    @ApiModelProperty("模型id/库表id/文件id")
    private Long relatedId;

    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataName;

    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;

    @ApiModelProperty("当前用户是否有阅读该资产的权限")
    private Boolean hasReadPermission;

    @ApiModelProperty("当前用户对该资产的申请次数")
    private Integer currentUserApplyCount;

}
