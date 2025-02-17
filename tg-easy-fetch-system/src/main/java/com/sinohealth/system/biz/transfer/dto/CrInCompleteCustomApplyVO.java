package com.sinohealth.system.biz.transfer.dto;

import com.sinohealth.system.domain.TgApplicationInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author Kuangcp
 * 2024-07-23 09:47
 */
@Data
public class CrInCompleteCustomApplyVO implements CrExcelVO {
    @ApiModelProperty("需求ID")
    private String applicationNo;

    @NotBlank(message = "需求名称不能为空")
    private String projectName;

    @ApiModelProperty("需求描述")
    @Size(max = 200, message = "需求描述长度超出限制")
    @Deprecated
    private String projectDesc;

    @NotBlank(message = "申请人不能为空")
    private String applicant;

    @NotBlank(message = "需求性质必填")
    private String requireAttr;

    @Size(max = 50, message = "合同编号长度超出限制")
    private String contractNo;

    // 通用模板 申请就填入 绑定的工作流
    /**
     * @see TgApplicationInfo#configType
     * @see TgApplicationInfo#workflowId
     */
    @ApiModelProperty("尚书台工作流")
    private String flowName;

    @NotBlank(message = "项目名称必填")
    private String project;

    @ApiModelProperty("常规交付周期")
    private String deliverTimeType;

    @ApiModelProperty("交付延期天数")
    private String deliverDelay;
    @NotBlank(message = "模板名称必填")
    private String templateName;

    @NotNull(message = "数据有效时间必填")
    private Date dataExpire;
}
