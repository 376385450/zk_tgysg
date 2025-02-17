package com.sinohealth.system.domain.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-03-29 15:39
 * @Desc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerticalModel {
    List<YAxis> y_axis;
    List<String> x_axis;
    Integer total;
    List<String> options;
    String tag;
    String name;

    @Data
    public static class YAxis {
        String yName;
        List<Integer> data = new ArrayList<>();
    }
}
