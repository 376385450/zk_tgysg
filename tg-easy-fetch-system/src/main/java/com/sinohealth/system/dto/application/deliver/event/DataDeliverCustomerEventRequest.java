package com.sinohealth.system.dto.application.deliver.event;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 20:09
 */
@Data
@JsonNaming
public class DataDeliverCustomerEventRequest implements Serializable {

    @ApiModelProperty("报表名/来源项目/分配人")
    private String searchKey;

    @ApiModelProperty("来源表id")
    private Long tableId;

    @ApiModelProperty("客户id")
    private Long authUserId;

    @NotNull(message = "资产id不能为空")
    @ApiModelProperty("资产id")
    private Long assetsId;

    @ApiModelProperty("状态")
    private Integer authStatus;

    @ApiModelProperty("分配方式： 1打包，0非打包")
    private Integer allocateType;

    private Integer page;

    private Integer size;
}
