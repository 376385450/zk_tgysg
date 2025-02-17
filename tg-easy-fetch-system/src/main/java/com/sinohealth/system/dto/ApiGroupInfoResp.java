package com.sinohealth.system.dto;

import com.sinohealth.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * 分组信息（给到 api接口基础信息 关联）视图对象 mall_package
 *
 * @author dataplatform
 * @date 2021-07-05
 */
@Data
@ApiModel("分组信息（给到 api接口基础信息 关联）视图对象")
public class ApiGroupInfoResp {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @ApiModelProperty("自增ID")
    private Long id;

    /**
     * 分组名称
     */
    @ApiModelProperty("分组名称")
    private String groupName;
    /**
     * 分组描述
     */
    @ApiModelProperty("分组描述")
    private String groupDesc;
    /**
     * 删除标志（0-未删除,1-已删除）
     */
    @ApiModelProperty("删除标志（0-未删除,1-已删除）")
    private String delStatus;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private Long createBy;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
