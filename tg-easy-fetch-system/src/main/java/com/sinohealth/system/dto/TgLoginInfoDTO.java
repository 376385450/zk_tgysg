package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sinohealth.common.annotation.Excel;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author zhangyanping
 * @date 2023/11/23 14:17
 */
@ApiModel(description = "活跃明细")
@Data
@EqualsAndHashCode(callSuper = false)
public class TgLoginInfoDTO {

    @Excel(name = "活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Excel(name = "活跃用户")
    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @Excel(name = "所属部门")
    @ApiModelProperty("所属组织")
    private String orgName;

    @Excel(name = "三级部门")
    @ApiModelProperty("三级部门")
    private String applicantDepartment;



    private String orgUserId;


}


