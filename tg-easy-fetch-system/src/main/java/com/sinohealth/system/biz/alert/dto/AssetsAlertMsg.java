package com.sinohealth.system.biz.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-16 17:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsAlertMsg {

    private Long applyId;

    private Long assetsId;

    private Integer tableVersion;

    private boolean success;
    private String tableName;

//    private Integer templateType;
}
