package com.sinohealth.system.biz.application.dto;

import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-01 09:58
 */
@Data
public class ParseSqlBatchRequest {
    private List<OneItem> applyList;
}
