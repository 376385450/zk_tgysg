package com.sinohealth.system.biz.project.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据排期
 *
 * @author Kuangcp
 * 2024-12-13 14:20
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_data_plan")
@Accessors(chain = true)
public class DataPlan extends Model<DataPlan> {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String bizType;

    private LocalDate day;

    private Boolean holiday;

    private String period;

    private String flowProcessType;

    /**
     * T+N 周期天数
     */
    private Integer duration;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
