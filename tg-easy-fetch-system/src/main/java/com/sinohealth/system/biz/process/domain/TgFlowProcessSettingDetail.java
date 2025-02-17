package com.sinohealth.system.biz.process.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 流程配置详细表
 *
 * @author zegnjun
 * 2024-08-05 15:17
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_flow_process_setting_detail")
public class TgFlowProcessSettingDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("基础设置id")
    private Long baseId;

    @ApiModelProperty("配置类别【qc、sop、交付、临时改数】")
    private String category;

    @ApiModelProperty("更新方式【全量、按品类、首次全量，之后品类】")
    private String updateType;

    @ApiModelProperty("是否开启需求数据对比")
    private Boolean planCompare;

    @ApiModelProperty("需求数据对比版本类型")
    private String planCompareCategory;

    @ApiModelProperty("是否开启底表数据对比")
    private Boolean tableDataCompare;

    @ApiModelProperty("底表数据对比版本类型")
    private String tableDataCompareCategory;

    @ApiModelProperty("是否开启qc")
    private Boolean assetsQc;

    @ApiModelProperty("是否推送powerbi")
    private Boolean pushPowerBi;

    @ApiModelProperty("附件信息")
    private String attach;
}
