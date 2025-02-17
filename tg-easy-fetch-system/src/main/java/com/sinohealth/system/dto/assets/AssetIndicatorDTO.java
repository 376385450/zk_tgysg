package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2023-10-31 14:16
 * @Desc
 */
@Data
@ApiModel("资产申请指标")
public class AssetIndicatorDTO {
    @ApiModelProperty("该资产申请总数量")
    private Integer applicationTotalNum;
    @ApiModelProperty("该资产最近周期申请总数量, 根据传入时间决定周期长短, 默认七天")
    private Integer lastPeriodApplicationTotalNum;
    @ApiModelProperty("该资产对应的申请总人数")
    private Integer applicantTotalNum;
    @ApiModelProperty("该资产对应的人均申请次数")
    private Double averageApplicationNumPerPerson;
    @ApiModelProperty("该资产申请通过率")
    private Double passRate;
    @ApiModelProperty("该资产申请最短审核时长, 单位 小时")
    private Double shortestAuditDuration;
    @ApiModelProperty("该资产申请平均审核时长, 单位 小时")
    private Double averageAuditDuration;
}
