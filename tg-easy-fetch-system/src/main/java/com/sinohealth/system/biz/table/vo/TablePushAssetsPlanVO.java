package com.sinohealth.system.biz.table.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 15:04
 */
@Data
@Builder
public class TablePushAssetsPlanVO {

    private Long id;

    /**
     * 计划对比旧版本
     */
    private Integer preVersion;
    /**
     * 期数版本
     */
    private String preVersionPeriod;
}
