package com.sinohealth.system.biz.application.dto;

import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-01 09:58
 */
@Data
public class ParseSqlRequest {
    private String sql;
    private String excludeSql;
}
