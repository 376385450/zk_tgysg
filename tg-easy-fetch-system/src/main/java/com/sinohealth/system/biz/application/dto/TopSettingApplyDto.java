package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.biz.application.constants.TopDurationType;
import com.sinohealth.system.biz.application.constants.TopPeriodTypeEnum;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top 设置 申请端
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-15 14:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSettingApplyDto {

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("时间组件")
    private FilterDTO.FilterItemDTO dateFilter;

    /**
     * @see TopPeriodTypeEnum
     */
    private Integer periodType;
    /**
     * 最近 N 月/季
     */
    private Integer lastDuration;
    /**
     * @see TopDurationType
     */
    private String durationType;


    @ApiModelProperty("目标对象字段")
    private Long targetField;
    @ApiModelProperty("排序字段")
    private Long sortField;

    @ApiModelProperty("分组字段")
    private List<Long> groupField;

    @ApiModelProperty("top数量")
    private Integer topNum;

    @ApiModelProperty("是否others打包")
    private Boolean othersPack;
}
