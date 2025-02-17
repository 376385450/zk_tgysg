package com.sinohealth.system.domain.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Author Rudolph
 * @Date 2023-03-29 15:39
 * @Desc
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieModel {

    List<Map<String, Object>> data;
    Long total;
    List<String> options;
    String tag;
    String name;

}
