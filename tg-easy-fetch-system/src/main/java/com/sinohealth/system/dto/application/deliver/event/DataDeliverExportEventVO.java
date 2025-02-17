package com.sinohealth.system.dto.application.deliver.event;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 15:40
 */
@Data
@ApiModel("交付导出、下载记录DTO")
public class DataDeliverExportEventVO implements Serializable {

    private Long id;

    @ApiModelProperty("分配方式： 打包，单份")
    private String allocateType;

    @ApiModelProperty("来源表ID")
    private Long baseTableId;

    @ApiModelProperty("来源表")
    private String tableName;

    @ApiModelProperty("来源表id")
    private Long tableId;

    @ApiModelProperty("对外数据表名字")
    private String outName;

    @ApiModelProperty("导出类型")
    private String exportType;

    @ApiModelProperty("项目名称")
    private String projectName;

    private String newProjectName;

    @ApiModelProperty("项目背景描述")
    private String projectDesc;

    @ApiModelProperty("下载时间")
    private String downloadTime;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("操作人id")
    private Long operatorId;

    @ApiModelProperty("打包明细")
    private List<DataDeliverExportEventItemDTO> itemList;

    @Data
    @ApiModel("DataDeliverExportEventDTO.DataDeliverExportEventItemDTO")
    public static class DataDeliverExportEventItemDTO implements Serializable {

        private String outName;

        private String projectName;

        private String tableName;

        @ApiModelProperty("来源表id")
        private Long tableId;

        private String exportType;

    }
}
