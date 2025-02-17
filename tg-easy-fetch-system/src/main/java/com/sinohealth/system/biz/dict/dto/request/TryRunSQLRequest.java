package com.sinohealth.system.biz.dict.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 19:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TryRunSQLRequest {

    private String sql;
    private Integer limit;
}
