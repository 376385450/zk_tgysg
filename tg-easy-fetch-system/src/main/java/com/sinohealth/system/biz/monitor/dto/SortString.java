package com.sinohealth.system.biz.monitor.dto;

import lombok.Data;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-11-30 11:11
 */
@Data
public class SortString {
    private long sort;
    private List<String> cols;

    public SortString(long sort, List<String> cols) {
        this.sort = sort;
        this.cols = cols;
    }
}
