package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.SortType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-25 17:06
 * @Desc
 */
@ApiModel(description = "/api/asset/query 接收的数据类")
@Data
public class AssetBackendPageQuery {
    private Long id;
    private Integer dirId;
    private AssetType type = AssetType.MODEL;
    private SortType sortType;
    private String searchContent;
    private Integer pageSize = 10;
    private Integer pageNum = 1;
    private String shelfState;
    private String sortField;
    private String sortRule;
    private String baseTableName;

    private String bizType;

    private List<Long> relatedIds;

    /**
     * 资产类型
     */
    @ApiModelProperty(name = "资产类型")
    private String assetBindingDataType;

    /**
     * 关联工作流编号
     */
    @ApiModelProperty(name = "关联工作流编号")
    private Long flowId;

    /**
     * 工作流关联记录编号
     */
    @ApiModelProperty(name = "工作流关联记录编号")
    private List<Long> flowRelatedIds;
}
