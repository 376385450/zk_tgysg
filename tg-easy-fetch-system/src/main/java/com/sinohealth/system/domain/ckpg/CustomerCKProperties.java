package com.sinohealth.system.domain.ckpg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-01 16:27
 */
@Data
@Component
@ConfigurationProperties("spring.datasource.druid.customerck")
public class CustomerCKProperties {
    private String type;
    private String url;
    private String username;
    private String password;
    private String database;
}
