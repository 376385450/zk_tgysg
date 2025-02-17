package com.sinohealth.system.biz.hook.processor;

import lombok.Data;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-07 17:31
 */
@Data
public class ServerTStat {
    private String ip;
    private List<TStat> stat;
}
