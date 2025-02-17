package com.sinohealth.system.biz.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@ApiModel("资产统计")
@Data
public class DataStatistics implements Serializable {

    @ApiModelProperty("资产总数")
    private Integer assetsAllCount;

    @ApiModelProperty("模型数量")
    private Integer moduleAllCount;

    @ApiModelProperty("库表数量")
    private Integer tableAllCount;

    @ApiModelProperty("文件数量")
    private Integer fileAllCount;

}
