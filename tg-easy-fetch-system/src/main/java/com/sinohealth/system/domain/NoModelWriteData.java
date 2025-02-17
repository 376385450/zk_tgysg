package com.sinohealth.system.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author Rudolph
 * @Date 2022-10-31 11:55
 * @Desc
 */
@Data
public class NoModelWriteData implements Serializable {
    private String fileName;
    private String[] headMap;
    private String[] dataStrMap;
    private List<Map<String, Object>> dataList;
}
