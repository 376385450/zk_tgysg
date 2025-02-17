package com.sinohealth.system.domain.label;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:25
 */
@Data
@TableName("tg_asset_label_relation")
@ApiModel("资产与标签的关联表")
@Accessors(chain = true)
public class TgAssetLabelRelation {

    @TableId
    private Long id;

    @ApiModelProperty("标签ID")
    private Integer labelId;

    @ApiModelProperty("资产ID")
    private Integer assetId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("是否删除，0：未删除，1：已删除")
    private Integer delFlag;

    @ApiModelProperty("删除时间")
    private Date deleteTime;
}
