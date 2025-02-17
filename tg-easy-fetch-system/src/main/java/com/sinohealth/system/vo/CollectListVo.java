package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 18:35
 */
@Data
@ApiModel("我的收藏列表响应体")
@Accessors(chain = true)
public class CollectListVo {

    @ApiModelProperty("资产ID")
    private Integer assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产类型，MODEL、TABLE、FILE")
    private String assetType;

    @ApiModelProperty("资产描述")
    private String assetDescription;

    @ApiModelProperty("资产提供方部门ID")
    private String assetProvider;

    @ApiModelProperty("资产提供方部门名称")
    private String assetProviderName;

    @ApiModelProperty("收藏时间")
    private String collectTime;

    @ApiModelProperty("所属目录")
    private String cataloguePath;

    @ApiModelProperty("所属目录中文")
    private String cataloguePathCn;

    @ApiModelProperty("资产标签")
    private String labelStr;

    @ApiModelProperty("是否可查看")
    private boolean isReadable;

    @ApiModelProperty("模型id/库表id/文件id")
    private Long relatedId;
}
