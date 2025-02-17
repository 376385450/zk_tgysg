package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 16:27
 */
@Data
public class TableSnapshotUpdateRequest {

    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty("说明")
    private String remark;

    private String versionPeriod;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;
}
