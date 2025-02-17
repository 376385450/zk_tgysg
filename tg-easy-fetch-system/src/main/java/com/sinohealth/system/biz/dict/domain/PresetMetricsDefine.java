package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-29 19:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_metrics_preset_dict")
@Accessors(chain = true)
public class PresetMetricsDefine {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long metricsId;

    private Long presetId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
