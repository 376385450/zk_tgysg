package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;


/**
 * 行业概念添加对象 data_business
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("行业概念添加对象")
public class MyApiQueryRequest implements Serializable {

    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("服务中文名称")
    private String apiName;

    @ApiModelProperty("服务英文名称")
    private String apiNameEn;

    @ApiModelProperty("所属组")
    private String groupId;

    @ApiModelProperty("开始选择时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeStart;

    @ApiModelProperty("结束选择时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeEnd;

    @ApiModelProperty("接口访问路径")
    private String requestPath;

    @ApiModelProperty("接口状态")
    private String apiStatus;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("接口状态 查看类型1.我的创建2.我的发布3.我的订阅")
    @NotNull(message = "viewType不能为null")
    private Integer viewType;/*查看类型1.我的创建2.我的发布3.我的订阅*/

}
