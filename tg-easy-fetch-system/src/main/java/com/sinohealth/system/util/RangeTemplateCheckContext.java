package com.sinohealth.system.util;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-02 13:53
 */
@Data
public class RangeTemplateCheckContext {

    private Set<String> sameTreeName;

    private Set<String> diffTreeName;

    private Set<String> categorySet;

    private String groupId;

    public void addCategory(Long categoryId, Integer level) {
        categorySet.add(groupId + "#" + categoryId + "#" + level);
    }

    public boolean hasSameCategoryId() {
        Map<Long, List<Long>> same = categorySet.stream().map(v -> {
            String[] arr = v.split("#");
            return arr[1];
        }).map(Long::parseLong).collect(Collectors.groupingBy(v -> v));
        return same.entrySet().stream().anyMatch(v -> v.getValue().size() > 1);
    }
}
