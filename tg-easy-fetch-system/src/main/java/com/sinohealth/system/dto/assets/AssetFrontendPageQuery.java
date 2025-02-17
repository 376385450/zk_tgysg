package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.SortType;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Rudolph
 * @Date 2023-08-25 17:06
 * @Desc
 */
@ApiModel(description = "/api/asset/front_tree_query 接收的数据类")
@Data
public class AssetFrontendPageQuery implements Serializable {
    private Integer dirId;
    private AssetType type;
    private SortType sortType;
    private String searchContent;
    private Integer pageSize = 10;
    private Integer pageNum = 1;
}
