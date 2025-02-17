package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Kuangcp
 * 2024-10-21 10:33
 */
@Data
public class UserDataAssetsVersionEditRequest {

    /**
     * 资产id
     */
    @NotNull(message = "id为空")
    private Long assetsId;

    /**
     * 版本
     */
    @NotNull(message = "版本为空")
    private Integer version;

    /**
     * 版本类型
     *
     * @see FlowProcessTypeEnum
     */
    @NotBlank(message = "参数为空")
    private String flowProcessType;
}
