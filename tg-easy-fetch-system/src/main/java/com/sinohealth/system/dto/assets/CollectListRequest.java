package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 17:40
 */
@Data
@ApiModel("我的收藏列表请求参数")
public class CollectListRequest {

    @ApiModelProperty("当前页")
    @NotNull(message = "当前页不能为空")
    private Integer pageNum;

    @ApiModelProperty("分页大小")
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产类型，MODEL、TABLE、FILE")
    private String assetType;
}
