package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/12/26
 */
@Data
public class PreviewApplyRequest implements Serializable {

    private TgApplicationInfo applicationInfo;

    private GetDataInfoRequestDTO requestDTO;

}
