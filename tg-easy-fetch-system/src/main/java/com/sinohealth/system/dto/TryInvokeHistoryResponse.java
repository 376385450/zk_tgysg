package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * 行业概念添加对象 data_business
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("行业概念添加对象")
public class TryInvokeHistoryResponse implements Serializable {

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

    @ApiModelProperty("订阅人数")
    private Integer subscribeCount;

    @ApiModelProperty("试用人")
    private String tryUserName;

    @ApiModelProperty("试用时间")
    private String tryUseTime;

    /** 请求参数（目前只支持pageSize和pageNum） */
    private String requestParam;

    /** 返回结果集（json格式保存） */
    private String returnResultJson;

    /** 展示调用失败原因：0成功，1服务器异常，2输入参数有误，3超过最大输入 **/
    private Integer invokeFailReason;

    @ApiModelProperty(" 0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）")
    private String apiStatus;

    @ApiModelProperty(" 请求方式")
    private String requestMethod;

}
