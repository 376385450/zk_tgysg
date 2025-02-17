package com.sinohealth.framework.resttempplate.config;

import com.sinohealth.framework.interceptor.TokenInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * RestTemplate客户端连接池配置
 *
 * @author phq
 * @since 2021-10-25
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RestTemplateConfig {

    @Resource
    private CloseableHttpClient httpClient;
    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.setInterceptors(Collections.singletonList(tokenInterceptor));
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory();
        rf.setHttpClient(httpClient);
        return rf;
    }

}
