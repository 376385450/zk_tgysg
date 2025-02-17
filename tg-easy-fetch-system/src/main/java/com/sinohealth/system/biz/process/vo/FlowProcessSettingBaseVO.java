package com.sinohealth.system.biz.process.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FlowProcessSettingBaseVO {
    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("流程名称【空时为系统自动生成】")
    private String name;

    @ApiModelProperty("计划执行时间")
    private String planExecutionTime;

    @ApiModelProperty("业务类型【业务线】")
    private String bizType;

    @ApiModelProperty("底表资产id")
    private Long tableAssetId;

    @ApiModelProperty("模板资产ids")
    private String modelAssetIds;

    @ApiModelProperty("类型【auto：自动、manual_operation：手动】")
    private String category;

    @ApiModelProperty("详细信息")
    private List<FlowProcessSettingDetailVO> details;
}
