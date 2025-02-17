package com.sinohealth.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 13:56
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "power-bi")
public class PowerBiPushProperties {

    private String host;
    private String db;
    private String user;
    private String pwd;

    /**
     * 项目表
     */
    private String projectTable;
    private String projectTableSchema;
}
