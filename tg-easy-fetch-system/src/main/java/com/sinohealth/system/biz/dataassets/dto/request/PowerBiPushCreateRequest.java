package com.sinohealth.system.biz.dataassets.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn 2024-05-25 09:55
 */
@Data
public class PowerBiPushCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    @Length(max = 64, message = "任务名称长度不能超过64")
    private String name;

    @NotEmpty(message = "关联资产不能为空")
    private List<Long> assetsIds;

    @ApiModelProperty("业务关联编号")
    private Long bizId;
}
