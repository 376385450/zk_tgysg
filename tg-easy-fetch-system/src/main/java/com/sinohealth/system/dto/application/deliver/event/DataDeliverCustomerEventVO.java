package com.sinohealth.system.dto.application.deliver.event;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 20:16
 */
@Data
@JsonNaming
public class DataDeliverCustomerEventVO implements Serializable {

    private Integer id;

    @ApiModelProperty("分配内容: 报表名/文件名")
    private String deliverName;

    @ApiModelProperty("分配方式： 打包，单份")
    private String allocateType;

    @ApiModelProperty("来源表id")
    private Long tableId;

    @ApiModelProperty("来源表")
    private String tableName;

    @ApiModelProperty("数据量")
    private Long dataTotal;
    @ApiModelProperty("导出次数")
    private Long exportTotal;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("客户名称")
    private String authUserName;

    private String authType;

    private Long nodeId;

    /**
     * BI图表分析id
     */
    private String extAnalysisId;

    private String icon;

    private Integer authStatus;

    private Integer authId;

    @ApiModelProperty("更新人名称")
    private String dataUpdater;

    @ApiModelProperty("更新人id")
    private Long dataUpdaterId;

    @ApiModelProperty("更新时间")
    private String dataUpdateTime;

    /**
     * 当icon为chart/dashboard时，有值，为bi报表关联的提数申请列表
     */
    @ApiModelProperty("关联的提数申请id列表")
    private List<Long> applicationIds;

    private List<DataDeliverCustomerEventVO> itemList;

}
