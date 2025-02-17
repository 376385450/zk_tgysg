package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 16:48
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TableSnapshotPageRequest extends PageRequest {

    private Long tableId;

    private String bizType;
    private String period;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    /**
     * @see com.sinohealth.system.biz.table.constants.TablePushStatusEnum
     */
    @ApiModelProperty("none 未推送资产 run 执行中 success failed")
    private String pushStatus;
}
