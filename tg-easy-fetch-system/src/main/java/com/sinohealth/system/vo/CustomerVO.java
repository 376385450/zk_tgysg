package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@ApiModel("客户")
@Data
public class CustomerVO implements Serializable {

    private Long id;

    private String shortName;

    private String fullName;

    private Integer customerType;

    private Integer customerStatus;

    private Integer relateProject;

    private Integer relateAsset;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    private String createdName;

    private String updatedName;

}
