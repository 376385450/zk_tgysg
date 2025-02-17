package com.sinohealth.system.biz.table.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 16:44
 */
@Data
public class TableDiffRequest {
    /**
     * 资源表编号
     */
    @NotNull(message = "资源表编号不可为空")
    @ApiModelProperty(value = "资源表编号")
    private Long tableId;

    /**
     * 新版数据编号
     */
    @ApiModelProperty(value = "新版数据编号")
    private Long newVersionId;

    /**
     * 旧版数据编号
     */
    @NotNull(message = "旧版数据编号不可为空")
    @ApiModelProperty(value = "旧版数据编号")
    private Long oldVersionId;

    /**
     * 计划编号
     */
    @ApiModelProperty(value = "计划编号")
    private Long planId;

    /**
     * 业务编号
     */
    @ApiModelProperty(value = "业务编号")
    private Long bizId;

    @ApiModelProperty(value = "回调地址")
    private String callBackUrl;

    @ApiModelProperty(value = "预计执行时间")
    private Date planExecuteTime;
}
