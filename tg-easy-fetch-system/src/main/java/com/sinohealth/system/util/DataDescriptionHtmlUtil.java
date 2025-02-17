package com.sinohealth.system.util;

import com.google.common.collect.Lists;
import com.sinohealth.system.domain.TgDataDescription;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据说明文档html工具
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-08 09:48
 */
public final class DataDescriptionHtmlUtil {

    public static String html(List<TgDataDescription> descriptions) {
        StringBuilder sb = new StringBuilder();
        for (TgDataDescription description : descriptions) {
            sb.append(tableHtml(description));
        }
        return sb.toString();
    }

    private static String tableHtml(TgDataDescription description) {
        List<List<String>> dataDescLines = description.getDataDesc().getList().stream().map(quota -> Lists.newArrayList(quota.getKey(), Optional.ofNullable(quota.getValue()).orElse(""))).collect(Collectors.toList());
        String dataDescTableHtml = tableHtml("数据说明", dataDescLines);
        List<List<String>> baseTargetLines = description.getBaseTarget().getList().stream().map(quota -> Lists.newArrayList(quota.getKey(), Optional.ofNullable(quota.getValue()).orElse(""))).collect(Collectors.toList());
        String baseTargetTableHtml = tableHtml("基础指标", baseTargetLines);
        return dataDescTableHtml + baseTargetTableHtml;
    }


    private static String tableHtml(String tableName, List<List<String>> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<tr>").append("<td colspan=\"2\" align=\"center\">").append(tableName).append("</td>").append("</tr>");
        // 表头
        for (int i = 0; i < lines.size(); i++) {
            List<String> line = lines.get(i);
            sb.append("<tr height=\"30\">");
            for (int j = 0; j < line.size(); ++j) {
                sb.append("<td style=\"width:200px;\">").append(line.get(j)).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

}
