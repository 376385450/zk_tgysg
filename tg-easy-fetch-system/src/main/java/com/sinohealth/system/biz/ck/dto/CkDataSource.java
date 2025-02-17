package com.sinohealth.system.biz.ck.dto;

import com.alibaba.druid.pool.DruidDataSource;
import com.sinohealth.common.config.DataConnection;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 15:01
 */
@Data
@AllArgsConstructor
public class CkDataSource {

    private String hostName;
    private String hostIp;

    private DruidDataSource source;

    private DataConnection conn;
}
