package com.sinohealth.framework.config;

/**
 * SmartInitializingSingleton 初始化后的bean 防止获取到代理类
 * @author kuangchengping@sinohealth.cn 
 * 2024-04-01 15:39
 */
//@Component
//public class ExceptionAlertAppenderContext implements ApplicationContextAware, SmartInitializingSingleton {
//
//    private static ApplicationContext applicationContext;
//    public static AlertService alertService;
//
//    @Override
//    public void afterSingletonsInstantiated() {
//        ExceptionAlertAppenderContext.alertService = applicationContext.getBean(AlertService.class);
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        ExceptionAlertAppenderContext.applicationContext = applicationContext;
//    }
//}
