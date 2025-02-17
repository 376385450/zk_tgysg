package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 11:15
 */
@Data
public class PowerBiPushBatchPageDTO {
    private Long id;

    private String name;
    /**
     * 模板id
     */
    private String templateId;

    /**
     * 模板名
     */
    private String templateName;

    /**
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    /**
     * 关联全流程名称
     */
    private String flowProcessName;

    /**
     * 全流程创建方式
     */
    private String flowProcessCategory;

    private Integer waitCnt;
    private Integer runCnt;
    private Integer failedCnt;
    private Integer successCnt;
    /**
     * 明细总数
     */
    private Integer detailCnt;

    // 不存储，从明细获取
    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private Boolean deleted;

    @ApiModelProperty("创建人")
    private String creatorName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("耗时")
    private String costTime;
}
