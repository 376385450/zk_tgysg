package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel
@Data
public class SystemVersionDto implements Serializable {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "版本号")
    private String version;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "版本说明")
    private String content;
    @ApiModelProperty(value = "升级时间")
    private Date createTime;
}
