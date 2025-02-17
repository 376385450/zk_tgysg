package com.sinohealth.system.dto.application;

import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zhangyanping
 * @date 2023/5/22 16:09
 */
@Data
@ToString
@ApiModel("我的审核-配置")
public class ApplicationConfigRequest {

    @ApiModelProperty("申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @ApiModelProperty("配置类型 0:SQL,1:工作流(默认0) 2 文件")
    private Integer type;

    @ApiModelProperty("工作流ID")
    private Integer workflowId;

    @ApiModelProperty("SQL文本")
    private String sql;

    @ApiModelProperty("是否启用定时任务")
    private Boolean enableScheduledTask;

    @ApiModelProperty("cron表达式")
    private String cron;

    @ApiModelProperty("关联字段库")
    private Boolean relateDict;

    /**
     * 需求个数
     */
    private Integer dataAmount;
    /**
     * 需求成本，单位P 1P=30min
     */
    private BigDecimal dataCost;

    private Integer dataCostMin;

    private FileAssetsUploadDTO assetsAttach;

}
