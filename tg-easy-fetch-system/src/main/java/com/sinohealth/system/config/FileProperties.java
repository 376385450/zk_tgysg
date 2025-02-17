package com.sinohealth.system.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-02 17:41
 */
@Data
@Configuration
@ConfigurationProperties()
public class FileProperties {

    @Value("${sinohealth.file.storageCode}")
    private String fileStorageCode;

    @Value("${sinohealth.file.obsPrefix}")
    private String obsPrefix;

    @Value("${sinohealth.file.ftpPrefix}")
    private String ftpPrefix;

    /**
     * 单位 Mib
     */
    @Value("${sinohealth.file.ftpAssetsMaxMib}")
    private Integer ftpAssetsMaxMib;

    @Value("${sinohealth.file.lockNum}")
    private Long lockNum;
}
