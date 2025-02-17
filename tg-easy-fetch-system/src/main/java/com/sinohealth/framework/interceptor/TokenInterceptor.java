package com.sinohealth.framework.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenInterceptor implements ClientHttpRequestInterceptor {

    @Value("${dsApi.token}")
    private String token;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//        System.err.println("进入RestTemplate拦截器");
        HttpHeaders headers = request.getHeaders();
        headers.add("accept-language", "zh-CN");
        headers.add("token", token);
        return execution.execute(request, body);
    }
}
