package com.sinohealth.system.dto.pool;

import lombok.Data;

import java.util.Date;


@Data
public class HistoryVersionResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String groupNames;

    private Date updateTime;

    private String infoUpdateContent;

    private String  remark;

    private String  apiVersion;

}
