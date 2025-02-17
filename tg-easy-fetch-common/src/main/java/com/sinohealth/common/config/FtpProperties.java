package com.sinohealth.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-22 16:05
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpProperties {

    private String server;

    private Integer port;

    private String user;

    private String password;
}
