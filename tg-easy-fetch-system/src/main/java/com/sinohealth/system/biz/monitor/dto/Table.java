package com.sinohealth.system.biz.monitor.dto;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-11-30 11:11
 */
@Data
public class Table {

    private List<String> header;
    private List<String> headerDetail;

    private List<List<String>> table = new ArrayList<>();

    public void addHeader(String... col) {
        if (Objects.isNull(col) || col.length == 0) {
            return;
        }
        this.header = Stream.of(col).collect(Collectors.toList());
    }

    public void addRow(List<String> row){
        table.add(row);
    }
    public void addRow(String... col) {
        if (Objects.isNull(col) || col.length == 0) {
            return;
        }
        List<String> row = Stream.of(col).collect(Collectors.toList());
        table.add(row);
    }

    public String renderHtml() {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        if (CollectionUtils.isNotEmpty(header)) {
            table.append("<thead>    <tr>");
            for (String head : header) {
                table.append("<th>").append(head).append("</th>");
            }
            table.append(" </tr>  </thead>");
        }

        int idx =1;
        table.append(" <tbody>   ");
        if (CollectionUtils.isNotEmpty(headerDetail)) {
            table.append("<tr>");
            for (String hd : headerDetail) {
                table.append("<td>").append(hd).append("</td>");
            }
            table.append("</tr>");
        }

        for (List<String> row : this.table) {
            table.append("<tr>");
            table.append("<td>【").append(idx).append("】</td>");
            for (String col : row) {
                table.append("<td>").append(col).append("</td>");
            }
            table.append("</tr>");
            idx++;
        }
        table.append(" </tbody></table>");
        return table.toString();
    }
}
