package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 15:52
 */
@Data
@TableName("tg_asset_user_relation")
@ApiModel("用户与资产的关联表")
@Accessors(chain = true)
public class TgAssetUserRelation {

    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("资产ID")
    private Long assetId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("是否收藏，1：是，0：否")
    private Integer isCollect;

    @ApiModelProperty
    private Date collectTime;

    @ApiModelProperty("转发数")
    private Integer forwardNum;

    @ApiModelProperty("浏览数")
    private Integer viewNum;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
