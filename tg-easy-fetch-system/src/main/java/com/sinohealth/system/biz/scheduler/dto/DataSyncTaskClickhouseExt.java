package com.sinohealth.system.biz.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-08 19:11
 */
@Data
public class DataSyncTaskClickhouseExt implements Serializable {

    /**
     * 排序键
     */
    private List<String> orderByList;

    /**
     * 集群名称
     */
    private String cluster;


}