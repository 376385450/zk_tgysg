package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;


/**
 * 我的api接口服务响应数据 data_business
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("我的api接口服务响应数据")
public class MyApiQueryResponse implements Serializable {

    /**  */
    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("api服务名称")
    private String apiName;

    @ApiModelProperty("接口英文名称")
    private String apiNameEn;

    @ApiModelProperty(" api版本号(内部维护字段)")
    private Long apiVersion;

    @ApiModelProperty(" 分组名称")
    private String groupName;

    @ApiModelProperty(" 接口访问路径")
    private String requestPath;

    @ApiModelProperty(" 0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）")
    private String apiStatus;

    @ApiModelProperty(" 更新人")
    private String updateBy;

    @ApiModelProperty(" 更新时间")
    private Date updateTime;

    @ApiModelProperty(" 创建时间")
    private Date createTime;

    @ApiModelProperty(" 创建人")
    private String createBy;


    @ApiModelProperty(" 请求方式")
    private String requestMethod;



    @ApiModelProperty(" 备注")
    private String remark;


    @ApiModelProperty(" 接口更新周期1-每天 2-每月 3-实时变更（参考目标库表的类型）")
    private String apiUpdateFrequency;


    @ApiModelProperty(" 接口关联组ID")
    private String groupId;

    @ApiModelProperty(" 创建人ID")
    private Long createId;


    @ApiModelProperty(" 接口变更内容")
    private String infoUpdateContent;

    @ApiModelProperty(" 发布失败的原因")
    private String publishFailMessage;

    @ApiModelProperty("订阅id")
    private String subscribeId;

    @ApiModelProperty(" pi接口类型1-静态 2-动态（参考表的类型）")
    private String apiType;


    @ApiModelProperty(" api版本号 由操作用户定义")
    private String apiVersionOut;


    @ApiModelProperty("申请订阅原因")
    private String subscribeReason;

    @ApiModelProperty("订阅状态：0--订阅成功  1--正在申请订阅   2-订阅失败")
    private String subscribeStatus;


}
