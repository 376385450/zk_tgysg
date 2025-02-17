package com.sinohealth.common.utils;


import com.sinohealth.common.config.HuaweiConfig;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Huawei OBS工具类
 *
 * @author wl
 * @version v1.0
 * @date 2021/7/19
 */
public class ObsUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObsUtil.class);

    private static HuaweiConfig.ObsRegistryVo config;

    public static HuaweiConfig.ObsRegistryVo getConfig() {
        return config;
    }

    public static void setConfig(HuaweiConfig.ObsRegistryVo config) {
        ObsUtil.config = config;
    }

    public static ObsClient getObsClient() {
        return initOBS();
    }

    public static void setObsClient(ObsClient obsClient) {
        ObsUtil.obsClient = obsClient;
    }

    /**
     * obs 工具客户端
     */
    private static ObsClient obsClient = null;

    /**
     * 初始化 obs 客户端
     *
     * @return
     */
    private static ObsClient initOBS() {
        if (obsClient == null) {
            ObsConfiguration obsConfiguration = new ObsConfiguration();
            obsConfiguration.setEndPoint(config.getEndpoint());
            obsClient = new ObsClient(config.getAccessKeyId(),config.getAccessKeySecret(),obsConfiguration);
        }
        return obsClient;
    }
}
