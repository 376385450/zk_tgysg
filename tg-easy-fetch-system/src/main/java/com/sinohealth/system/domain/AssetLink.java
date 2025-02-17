package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author shallwetalk
 * @Date 2024/3/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_asset_link")
@ApiModel("关联链接")
public class AssetLink {

    @TableId
    private Long id;

    private Long assetId;

    // 1-资产关联 2-操作指引
    private Integer linkType;

    @ApiModelProperty("链接url")
    private String linkUrl;

    @ApiModelProperty("链接名")
    private String linkName;

}
