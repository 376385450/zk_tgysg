package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDataAssetsVersionPageRequest extends PageRequest {

    @ApiModelProperty("资产id")
    private Long assetsId;
}
