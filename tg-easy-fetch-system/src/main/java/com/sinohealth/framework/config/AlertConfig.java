package com.sinohealth.framework.config;

import com.sinohealth.common.alert.AlertTemplate;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.spi.alert.AlertChannelType;
import com.sinohealth.common.spi.alert.AlertPluginInstance;
import com.sinohealth.common.spi.alert.AlertPluginManager;
import com.sinohealth.system.config.AlertBizType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-15 10:51
 */
@Configuration
public class AlertConfig {

    @Autowired
    private AppProperties appProperties;

    /**
     * 业务告警
     *
     * <a href="https://developer.work.weixin.qq.com/document/path/91770">企业微信机器人文档</a>
     */
    @Bean(AlertBizType.BIZ)
    public AlertTemplate bizAlertTemplate(AlertPluginManager alertPluginManager) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setAlertChannelType(AlertChannelType.wechatrobot);
        Map<String, String> map = new HashMap<>();
        map.put("webHook", "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + appProperties.getWxRobot());
        alertPluginInstance.setPluginInstanceParams(map);
        return new AlertTemplate(alertPluginManager, alertPluginInstance);
    }

    /**
     * 开发用机器人
     */
    @Bean(AlertBizType.DEV)
    public AlertTemplate devAlertTemplate(AlertPluginManager alertPluginManager) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setAlertChannelType(AlertChannelType.wechatrobot);
        Map<String, String> map = new HashMap<>();
        map.put("webHook", "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + appProperties.getWxDevRobot());
        alertPluginInstance.setPluginInstanceParams(map);
        return new AlertTemplate(alertPluginManager, alertPluginInstance);
    }

}
