package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-08-13 14:19
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TablePushDetailPageRequest extends FlowAssetsPageRequest {

    private Long tableId;
    private Integer version;
}
