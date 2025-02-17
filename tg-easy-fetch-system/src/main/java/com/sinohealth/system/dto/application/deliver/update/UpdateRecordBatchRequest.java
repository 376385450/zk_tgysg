package com.sinohealth.system.dto.application.deliver.update;

import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-10 15:02
 */
@Data
public class UpdateRecordBatchRequest {
    /**
     * 资产id集合
     */
    private List<Long> applyIds;
}
