package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
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
@TableName(value = "tg_assets_flow_batch")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsFlowBatch extends Model<AssetsFlowBatch> implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long bizId;

    private Long autoId;

    private String name;
    /**
     * @see AssetsQcBatch#id
     */
    private Long qcBatchId;

    private String remark;

    private String bizType;

    private String templateIds;

    private String state;

    private String period;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    private Boolean needQc;

    private LocalDateTime expectTime;
    private LocalDateTime finishTime;

    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
