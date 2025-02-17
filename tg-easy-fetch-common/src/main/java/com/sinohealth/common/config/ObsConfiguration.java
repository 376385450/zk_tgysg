package com.sinohealth.common.config;

import com.sinohealth.common.utils.ObsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Huawei OBS 初始化配置
 *
 * @author wl
 * @version v1.0
 * @date 2021/7/19
 */
@Configuration
public class ObsConfiguration {

    @Autowired
    private HuaweiConfig config;


    @Bean
    public void initOssBootConfiguration() {
        ObsUtil.setConfig(config.getRegistry());
    }
}
