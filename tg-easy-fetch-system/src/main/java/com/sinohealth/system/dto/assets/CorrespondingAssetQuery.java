package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.SortType;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Rudolph
 * @Date 2023-10-30 15:04
 * @Desc
 */
@ApiModel(description = "/api/asset/corresponding_asset_query 接收的数据类")
@Data
public class CorrespondingAssetQuery {
    @NotNull(message = "资产id必填")
    private Long assetId;
    private Integer dirId;
    private AssetType type;
    private SortType sortType;
    private String searchContent;
    private Integer pageSize = 10;
    private Integer pageNum = 1;
}

