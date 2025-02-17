package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 10:34
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class AssetsQcPageRequest extends PageRequest {

    private String searchName;

    private String state;

}
