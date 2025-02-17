package com.sinohealth.system.dto.auditprocess;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-05-20 17:17
 * @Desc
 */
@Data
public class SimpleAuditNodeStatus {
    private String nodeName;
    private String nodeRealHandler;
    private String HandleTime;
}
