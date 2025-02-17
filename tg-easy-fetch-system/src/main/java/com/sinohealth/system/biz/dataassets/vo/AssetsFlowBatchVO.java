package com.sinohealth.system.biz.dataassets.vo;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
public class AssetsFlowBatchVO {
    private Long id;

    private Long bizId;

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

    /**
     * 详细信息
     */
    private List<AssetsFlowBatchDetailVO> details;
}
