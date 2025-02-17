package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 20:58
 */
@Data
@JsonNaming
public class TgDeliverCustomerRecordDTO implements Serializable {

    private Integer id;

    private Long tableId;

    private String tableName;

    /**
     * 1打包交付，0非打包交付
     */
    private Integer allocateType;

    private Long allocateUserId;

    private String allocateUserName;

    private String authType;

    private String projectName;

    private String packName;

    private Integer parentRecordId;

    private Long nodeId;

    private String icon;

    private Long createBy;

    private Date createTime;

    private Integer authId;

    /**
     * com.sinohealth.common.enums.StatusTypeEnum
     */
    private Integer authStatus;
}
