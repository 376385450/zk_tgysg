package com.sinohealth.system.dto.application.deliver.update;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-10 14:24
 */
@Data
public class UpdateRecordQueryRequest extends PageRequest {

    @NotNull(message = "资产id不能为空")
    private Long assetsId;
}
