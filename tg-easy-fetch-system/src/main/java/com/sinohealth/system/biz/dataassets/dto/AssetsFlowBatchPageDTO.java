package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-07-15 10:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsFlowBatchPageDTO {

    private Long id;

    private String name;

    private String remark;

    private String bizType;

    private String templateName;

    private String state;

    private LocalDateTime expectTime;

    // 不存储，从明细获取
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    /**
     * 耗时
     */
    private String costTime;

    private Integer finishCnt;

    private String period;

    /**
     * 关联全流程名称
     */
    private String flowProcessName;

    private String flowProcessCategory;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    /**
     * 明细总数
     */
    private Integer detailCnt;

    private String creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
