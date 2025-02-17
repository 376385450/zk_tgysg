package com.sinohealth.system.dto.auditprocess;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.system.dto.application.Notice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-16 11:57
 * @Desc
 */
@ApiModel(description = "流程审核节点详细信息")
@Data
public class ProcessNodeDetailDto {
    @ApiModelProperty("节点名称")
    @Size(max = 100, message = "名称长度超出限制")
    private String name;
    @ApiModelProperty("审核人ID")
    private List<Long> handlers;
    @ApiModelProperty("是否超时")
    private String isTtl;
    @ApiModelProperty("超时时间")
    private String ttl;
    @ApiModelProperty("是否通知")
    private String isNotices;
    @ApiModelProperty("异常处理")
    private String abnormalHandle;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("状态评价")
    private String statusComment;
    @ApiModelProperty("消息通知信息(JSON序列化)")
    @JsonIgnore
    private String noticesJson;
    @ApiModelProperty("消息通知信息(JSON序列化)")
    @TableField(exist = false)
    private List<Notice> noticesInfo;
}
