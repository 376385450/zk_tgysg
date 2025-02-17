package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.AutoFlowTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-07-15 11:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_flow_auto_batch")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsFlowAutoBatch extends Model<AssetsFlowAutoBatch> implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String bizType;

    /**
     * 多值 ,
     */
    private String templateIds;
    /**
     * 多值 ,
     */
    private String applyIds;

    private AutoFlowTypeEnum autoType;

    private String cron;

    private Long jobId;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    private String projectName;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    private String requireTimeType;

    /**
     * 交付周期
     *
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    @ApiModelProperty("工作流名称")
    private String flowName;

    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
