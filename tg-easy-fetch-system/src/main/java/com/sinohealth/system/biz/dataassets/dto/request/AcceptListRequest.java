package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-21 15:01
 */
@Data
public class AcceptListRequest {

    private Long applicationId;

    private Long assetsId;

    /**
     * 如果资产有版本，存储资产版本
     */
    private Integer version;

}
