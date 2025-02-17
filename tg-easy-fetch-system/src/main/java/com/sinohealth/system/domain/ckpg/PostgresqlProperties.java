package com.sinohealth.system.domain.ckpg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author Rudolph
 * @Date 2022-07-26 17:58
 * @Desc
 */
@Data
@Component
@ConfigurationProperties("spring.datasource.druid.postgresql")
public class PostgresqlProperties {
    private String type;
    private String url;
    private String username;
    private String password;
}
