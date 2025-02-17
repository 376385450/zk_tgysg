package com.sinohealth.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhangyanping
 * @date 2023/11/21 13:41
 */
@Data
public class DataStatisticsCommonDTO {
    private String name;
    private List<DataStatisticsDTO> dataList;

    public DataStatisticsCommonDTO(String name, List<DataStatisticsDTO> dataList) {
        this.name = name;
        this.dataList = dataList;
    }
}
