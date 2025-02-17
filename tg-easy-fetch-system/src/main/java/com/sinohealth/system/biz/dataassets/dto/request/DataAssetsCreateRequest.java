package com.sinohealth.system.biz.dataassets.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 18:10
 */
@Data
@Builder
public class DataAssetsCreateRequest {

    @ApiModelProperty("源 申请id")
    private Long srcApplicationId;

    // 申请， 原始表同步
    private String srcType;

    // 构造的SQL 申请构造，数据同步
    private String assetsSql;

    private String assetTableName;
}
