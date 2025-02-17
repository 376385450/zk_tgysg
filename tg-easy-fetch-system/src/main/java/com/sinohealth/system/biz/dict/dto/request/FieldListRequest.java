package com.sinohealth.system.biz.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-18 09:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldListRequest {
    @ApiModelProperty("id集合")
    private List<Long> ids;
}
