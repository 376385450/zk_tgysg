package com.sinohealth.system.dto.assets;

import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 16:55
 */
@Data
public class AssetsFormDataQuery {

    @NotNull(message = "applyId")
    private Long applyId;

    private GetDataInfoRequestDTO filter;
}
