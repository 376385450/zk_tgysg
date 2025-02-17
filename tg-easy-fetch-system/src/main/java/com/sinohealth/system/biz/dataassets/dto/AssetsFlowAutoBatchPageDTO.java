package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.dataassets.constant.AutoFlowTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Kuangcp
 * 2024-07-15 10:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsFlowAutoBatchPageDTO {

    private Long id;

    private String name;

    private AutoFlowTypeEnum autoType;

    private Integer relateSize;

    private String cron;

    private String cronCN;
    /**
     * 计划执行时间
     */
    private Date planTime;

    private String creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
