package com.sinohealth.system.biz.application.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-10 15:17
 */
@Data
public class TableUpdateTimeEntity {

    private Date updateTime;

    private String ckTableName;
}
