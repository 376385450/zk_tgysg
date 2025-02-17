package com.sinohealth.system.biz.process.dto;

import com.sinohealth.common.enums.process.FlowProcessUpdateType;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@ToString
public class FlowProcessSaveSettingDetailRequest {
    @ApiModelProperty(name = "主键")
    private Long id;

    /**
     * @see FlowProcessTypeEnum
     */
    @ApiModelProperty(name = "版本类型【qc、sop、交付、临时改数】")
    @NotBlank(message = "版本类型不可为空")
    private String category;

    /**
     * @see FlowProcessUpdateType
     */
    @ApiModelProperty(name = "更新方式")
    @NotBlank(message = "更新方式不可为空")
    private String updateType;

    @ApiModelProperty(name = "是否开启需求数据对比")
    @NotNull(message = "是否开启需求数据对比不可为空")
    private Boolean planCompare;

    @ApiModelProperty(name = "需求数据对比-版本类型")
    private String planCompareCategory;

    @ApiModelProperty(name = "是否开启底表数据对比")
    @NotNull(message = "是否开启底表数据对比不可为空")
    private Boolean tableDataCompare;

    @ApiModelProperty(name = "底表数据对比-版本类型")
    private String tableDataCompareCategory;

    @ApiModelProperty(name = "是否开启项目qc")
    @NotNull(message = "是否开启项目qc不可为空")
    private Boolean assetsQc;

    @ApiModelProperty(name = "是否发起PowerBI推数")
    @NotNull(message = "是否发起PowerBI推数不可为空")
    private Boolean pushPowerBi;

    @ApiModelProperty(name = "关联品类")
    private List<String> prodCodes;
}
