package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.domain.TgTemplateInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-29 21:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TemplatePageDto extends TgTemplateInfo {

    @ApiModelProperty("业务分类显示名")
    private String businessType;

    @ApiModelProperty("资产id")
    private Long newAssetId;
}
