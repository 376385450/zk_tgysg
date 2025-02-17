package com.sinohealth.system.dto.table_manage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-20 15:08
 * @Desc
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataRangePageDto {
    private Integer total;
    private Integer pageNum;
    private Integer pageSize;
    private List<String> content;
    private String sql;

    public DataRangePageDto(Integer total, Integer pageNum, Integer pageSize, List<String> content) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.content = content;
    }
}
