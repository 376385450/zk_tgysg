package com.sinohealth.system.biz.doc.dto;

import com.sinohealth.system.domain.TgDocInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-29 21:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DocInfoPageDto extends TgDocInfo {


    @ApiModelProperty("业务分类显示名")
    private String businessType;
}
