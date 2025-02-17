package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 表图片信息
 *
 * @author linkaiwei
 * @date 2021/11/30 15:30
 * @since 1.6.1.2
 */
@ApiModel("表图片信息")
@Data
@Accessors(chain = true)
public class TableInfoImageDTO implements Serializable {

    @ApiModelProperty("表图片ID")
    private Long id;

    @ApiModelProperty("表ID")
    private Long tableId;

    @ApiModelProperty("表图片路径")
    private String imagePath;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人ID")
    private Long createBy;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人ID")
    private Long updateBy;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态：0删除，1正常，2停用")
    private Integer status;

}
