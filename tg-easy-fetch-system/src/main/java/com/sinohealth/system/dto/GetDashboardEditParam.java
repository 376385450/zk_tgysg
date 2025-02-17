package com.sinohealth.system.dto;

import com.sinohealth.common.constant.InfoConstants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetDashboardEditParam {
    @NotEmpty(message = InfoConstants.HANDLE_CHAIN_NOT_EMPTY)
    List<Long> assetsIds;
}
