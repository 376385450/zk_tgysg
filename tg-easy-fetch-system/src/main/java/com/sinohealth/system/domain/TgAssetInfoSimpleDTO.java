package com.sinohealth.system.domain;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.ResourceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Rudolph
 * @Date 2023-08-15 16:06
 * @Desc
 */
@ApiModel(description = "资产信息列表DTO")
@Data
public class TgAssetInfoSimpleDTO {

    @ApiModelProperty("资产id")
    private Long id;
    @ApiModelProperty("资产名称")
    private String assetName;
    @ApiModelProperty("文档类型")
    private String fileType;
    @ApiModelProperty("模型/库表/文件")
    private AssetType type;
    @ApiModelProperty("模型id/库表id/文件id")
    private Long relatedId;
    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataType;
    @ApiModelProperty("所属目录id")
    private Integer assetMenuId;
    @ApiModelProperty("一二级所属目录")
    private String menuName;
    @ApiModelProperty("全路径所属目录")
    private String fullMenuName;
    @ApiModelProperty("流程id")
    private Long processId;
    @ApiModelProperty("表英文名")
    private String tableName;
    @ApiModelProperty("关联表英文名")
    private String relatedTableName;
    @ApiModelProperty("资源类型： 表管理(TABLE_MANAGEMENT), 元数据(METADATA_MANAGEMENT)")
    private ResourceType resourceType;
    @ApiModelProperty("资产负责人")
    private String assetManagerName;
    @ApiModelProperty("资产提供方id")
    private String assetProvider;

    @ApiModelProperty("资产提供方名称")
    private String assetProviderName;

    /**
     * 库表版本
     */
    private Integer version;
    /**
     * 数据同步时间
     */
    private LocalDateTime syncTime;

    @ApiModelProperty("上架状态")
    private String shelfState;
    @ApiModelProperty("更新人")
    private String updater;
    @ApiModelProperty("更新时间")
    private String updateTime;
    @ApiModelProperty("排序")
    private Long assetSort;
    @ApiModelProperty("申请次数")
    private Integer applyTimes;
    @ApiModelProperty("申请成功次数")
    private Integer applySucceedTimes;
    @ApiModelProperty("阅读次数")
    private Integer readTimes;
    @ApiModelProperty("资产类型")
    private Integer templateType;
    @ApiModelProperty
    private String desc;
    @ApiModelProperty("下载PDF文件次数")
    private Integer downloadPdfTimes;
    @ApiModelProperty("下载源文件次数")
    private Integer downloadSourceFileTimes;
    @ApiModelProperty("工作流名称")
    private String flowName;
}
