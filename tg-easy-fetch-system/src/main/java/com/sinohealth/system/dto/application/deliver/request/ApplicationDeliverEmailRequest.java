package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 10:14
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ApiModel("数据交付email请求")
public class ApplicationDeliverEmailRequest extends DeliverPackBaseReq implements Serializable {

    @ApiModelProperty("邮件标题")
    @NotBlank(message = "邮件标题不能为空")
    private String emailTitle;

    @ApiModelProperty("邮件内容")
    @NotBlank(message = "邮件内容不能为空")
    private String emailBody;

    @ApiModelProperty("收件人")
    @NotEmpty(message = "收件人不能为空")
    private List<String> emailReceivers;

    private Long userId;
}
