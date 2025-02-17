package com.sinohealth.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 不要放入controller包里面，k8s心跳检测会打无意义的日志
 * @author kuangchengping@sinohealth.cn
 * 2023-02-28 16:42
 */
@RestController()
@RequestMapping("/internal")
public class HealthController {

    @RequestMapping("/ping")
    public String ping(){
        return "OK";
    }
}
