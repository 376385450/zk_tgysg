package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @Author Rudolph
 * @Date 2022-07-29 9:34
 * @Desc
 */

@ApiModel(description = "任务实体")
@Data
@TableName(value = "tg_sync_task_queue")
public class TgSyncTask extends Model<TgSyncTask> {
    @ApiModelProperty("自增主键ID")
    private Long id;
    @ApiModelProperty("资产ID")
    private Long assetsId;

    @ApiModelProperty("涉及到的表名")
    private String tableName;
    @ApiModelProperty("申请同步时间")
    private Date applySyncTime;
    @ApiModelProperty("预估同步时间")
    private Date estimateSyncTime;
    @ApiModelProperty("同步初始时间")
    private Date actualSyncTime;
    @ApiModelProperty("同步完成时间")
    private Date actualSyncDoneTime;
    @ApiModelProperty("上次数据量")
    private Long lastSyncVolume;
    @ApiModelProperty("当次数据量")
    private Long latestSyncVolume;
    @ApiModelProperty("同步状态")
    private Integer syncState;
    @ApiModelProperty("同步备注")
    private String syncComment;

}
