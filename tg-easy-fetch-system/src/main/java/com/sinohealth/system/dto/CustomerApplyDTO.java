package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-19 15:52
 */
@Data
@JsonNaming
public class CustomerApplyDTO implements Serializable {

    @ApiModelProperty("授权资产id")
    private Long id;

    @ApiModelProperty("目录树id")
    private Long dirId;

    private String assetsId;

    @ApiModelProperty("报表权限：1:查看;2下载")
    private String authType;

    @ApiModelProperty("类型")
    private String icon;

    @ApiModelProperty("状态：1正常，2禁用")
    private Integer status;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("业务id")
    private Long nodeId;

    private Long userId;

    private String customer;

    /**
     * 前端用。。。
     * 已分配为true，未分配未false
     */
    private Boolean disabled;

    private List<CustomerApplyDTO> children;
}
