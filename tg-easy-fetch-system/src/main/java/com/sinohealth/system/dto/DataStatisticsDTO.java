package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


/**
 * @author zhangyanping
 * @date 2023/11/21 13:41
 */
@Data
public class DataStatisticsDTO implements Serializable {
    private String date;
    private Integer count;
    private String orgUserIds;
    private String type;
    private String name;

    public DataStatisticsDTO() {
    }

    public DataStatisticsDTO(String date) {
        this.date = date;
        this.count = 0;
    }

    public DataStatisticsDTO(String type, Integer count) {
        this.count = count;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataStatisticsDTO that = (DataStatisticsDTO) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
