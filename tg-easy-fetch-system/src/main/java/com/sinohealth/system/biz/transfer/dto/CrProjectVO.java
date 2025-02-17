package com.sinohealth.system.biz.transfer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-08 11:31
 */
@Data
public class CrProjectVO {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    /**
     * 项目背景
     */
    private String description;

    @NotBlank(message = "客户不能为空")
    private String customer;

    @NotBlank(message = "项目经理不能为空")
    private String projectManager;

    private String cooperator;
}
