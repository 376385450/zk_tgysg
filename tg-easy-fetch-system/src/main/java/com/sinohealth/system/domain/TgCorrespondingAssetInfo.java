package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

/**
 * @Author Rudolph
 * @Date 2023-10-30 14:09
 * @Desc
 */
@ApiModel(description = "关联资产实体")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TgCorrespondingAssetInfo {

    @ApiModelProperty("资产ID")
    private Long id;

    @ApiModelProperty("模型(MODEL)/库表(TABLE)/文件(FILE)")
    private AssetType type;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("当前用户是否申请过该资产")
    private Boolean hasApplied;

    @ApiModelProperty("当前用户对该资产的申请次数")
    private Integer currentUserApplyCount;
}
