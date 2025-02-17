package com.sinohealth.system.biz.application.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top 设置 模板端
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-15 14:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSettingTempDto {

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("目标对象字段")
    private List<Long> targetFields;

    @ApiModelProperty("排序字段")
    private List<Long> sortFields;

    @ApiModelProperty("分组字段")
    private List<Long> groupFields;

    @ApiModelProperty("top关联字段 展示于资产表")
    private Long topFieldId;
}
