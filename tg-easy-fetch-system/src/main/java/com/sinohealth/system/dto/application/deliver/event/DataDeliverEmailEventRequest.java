package com.sinohealth.system.dto.application.deliver.event;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 16:06
 */
@Data
@ApiModel("交付email记录查询")
public class DataDeliverEmailEventRequest implements Serializable {

    @ApiModelProperty("报表名/来源项目/邮箱")
    private String searchKey;

    @ApiModelProperty("来源表id")
    private Long tableId;

    @NotNull(message = "资产id不能为空")
    @ApiModelProperty("资产id")
    private Long assetsId;

    @ApiModelProperty("分配方式： 1打包，0非打包")
    private Integer allocateType;

    private Integer page;

    private Integer size;

}
