package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.utils.SecurityUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("TgCogradientInfoDto")
public class TgCogradientInfoDto {

    @ApiModelProperty("字段ID")
    private Long id;

    @ApiModelProperty("表id")
    private Long tableId;

    @ApiModelProperty("目标表名称")
    private String tableName;

    @ApiModelProperty("ds流程id")
   private Integer processId;

    @ApiModelProperty("ds定时器id")
   private  Integer  scheduleId;

    @ApiModelProperty("流程json字段")
   private String processDefinitionJson;

    @ApiModelProperty("任务名称")
    private String name;

    @ApiModelProperty("调度频率")
    private String crontab;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建用户")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新用户")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态：0下线，1上线")
    private Integer status;

    @ApiModelProperty("后续三次触发时间")
    private List<Date> exeTime;

    @ApiModelProperty("已同步次数")
    private int syncCnt;

    @ApiModelProperty("最新同步状态")
    private Integer syncStatus;

    @ApiModelProperty("调度表达式")
    private String cron;

    @ApiModelProperty("创建用户带组织架构")
    private String createByOri;


}
