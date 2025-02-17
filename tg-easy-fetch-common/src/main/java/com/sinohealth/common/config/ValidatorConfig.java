package com.sinohealth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-05 20:26
 */
@Configuration
public class ValidatorConfig {

    @Bean
    public Validator build(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
