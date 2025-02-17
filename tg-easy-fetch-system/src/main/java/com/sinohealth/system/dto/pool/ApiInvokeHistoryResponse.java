package com.sinohealth.system.dto.pool;

import lombok.Data;

import java.util.Date;


@Data
public class ApiInvokeHistoryResponse {

    private Long id;

    private Long userId;

    private String apiName;

    private String apiNameEn;

    private String groupNames;

    private String requestPath;

    private Integer invokeFailReason;

    private Long executeTime;

    private Date updateTime;

    private String infoUpdateContent;

    private String  remark;

    private String  apiVersion;

    private String userName;
}
