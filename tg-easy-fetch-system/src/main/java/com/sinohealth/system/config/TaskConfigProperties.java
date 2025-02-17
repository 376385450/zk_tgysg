package com.sinohealth.system.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhangyanping
 * @date 2023/6/26 13:49
 */
 
@Data
@Component
public class TaskConfigProperties {


    @Value("${task.tableId}")
    private Long tableId;

    @Value("${task.source-db-type}")
    private String sourceDbType;

    @Value("${task.source-db-id}")
    private String sourceDb;

    @Value("${task.target-db-type}")
    private String targetDbType;

    @Value("${task.target-db-id}")
    private String targetDb;

    @Value("${task.tenantId}")
    private String tenantId;
}