package com.sinohealth.system.biz.process.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 流程配置基础表
 *
 * @author zegnjun
 * 2024-08-05 15:17
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_flow_process_setting_base")
public class TgFlowProcessSettingBase implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
