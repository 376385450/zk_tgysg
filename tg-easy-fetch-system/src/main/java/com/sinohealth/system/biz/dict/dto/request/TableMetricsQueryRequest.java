package com.sinohealth.system.biz.dict.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-01 09:42
 */
@Data
public class TableMetricsQueryRequest {

    @NotEmpty(message = "表id缺失")
    private List<Long> tableId;

    private String searchContent;
}
