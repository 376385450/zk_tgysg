package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.constant.CommonConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-05 21:48
 */
@Data
public class DataDirRequest {
    private Long id;
    private Integer searchStatus;
    private String searchProjectName;
    private String searchBaseTable;
    private Long searchBaseTableId;
    // expire normal
    private String expireType;
    private String clientNames;
    private Integer requireTimeType;
    private Integer requireAttr;
    @ApiModelProperty("表id")
    private Long baseTableId;

    private Long templateId;

    private String icon;

    // 内部使用
    /**
     * @see CommonConstants#MY_DATA_DIR
     */
    private Integer target;

    private Long applicantId;

}
