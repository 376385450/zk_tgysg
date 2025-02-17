package com.sinohealth.system.dto.application;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-19 14:34
 * @Desc
 */

@Data
public class SqlParts {
    String colsContent;
    String groupByContent;
    String baseTable;
    List<String> metricsContent = new ArrayList<>();
    List<String> joinsContent = new ArrayList<>();
    List<Long> joinWay = new ArrayList<>();
    List<String> DataRangeContent = new ArrayList<>();
    List<String> GroupHavingConetent = new ArrayList<>();
}
