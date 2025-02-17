package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.StaffType;
import com.sinohealth.common.enums.WhitlistServiceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-15 10:01
 * @Desc
 */
@ApiModel(description = "资产信息表(tg_asset_whitelist_info)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_asset_whitelist_info")
@EqualsAndHashCode(callSuper = false)
public class TgAssetWhitelistInfo extends Model<TgAssetWhitelistInfo> {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模型/库表/文件")
    private AssetType type;

    @ApiModelProperty("资产id")
    @NotNull(message = "资产id必填")
    private Long assetId;

    @ApiModelProperty("模型id/库表id/文件id")
    @NotNull(message = "模型id/库表id/文件id必填")
    private Long relatedId;

    @ApiModelProperty("部门/人员")
    private StaffType staffType;

    @ApiModelProperty("部门id/人员id")
    private String staffId;

    @ApiModelProperty("资产可见范围/服务申请流程")
    private WhitlistServiceType serviceType;

    @ApiModelProperty("资产开放服务JSON")
    @TableField(exist = false)
    private List<AssetPermissionType> assetOpenServices;

    @ApiModelProperty("资产开放服务JSON")
    private String assetOpenServicesJson;

    @ApiModelProperty("有效期")
    private String expirationDate;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgAssetWhitelistInfo newInstance() {
        return new TgAssetWhitelistInfo();
    }
}
