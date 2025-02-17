package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Jingjun
 * @since 2021/4/30
 */
@Data
@ApiModel("PasswordDto")
public class PasswordDto {

    @NotNull
    private String password;

    private Long userId;

    private  String keyword;
}
