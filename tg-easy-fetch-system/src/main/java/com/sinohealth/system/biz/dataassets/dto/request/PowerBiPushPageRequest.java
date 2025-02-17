package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 09:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PowerBiPushPageRequest extends PageRequest {
    private String pushName;
    private String template;
    private String state;
}
