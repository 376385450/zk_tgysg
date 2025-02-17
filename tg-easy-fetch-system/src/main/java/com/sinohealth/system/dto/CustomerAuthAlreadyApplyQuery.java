package com.sinohealth.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-02 11:30
 */
@Data
public class CustomerAuthAlreadyApplyQuery {

    private List<CustomerAuthAlreadyApplyItem> items;

    @Data
    public static class CustomerAuthAlreadyApplyItem {

        private String icon;

        private List<Long> nodeIds;
    }

    public static CustomerAuthAlreadyApplyQuery build(Map<String, List<Long>> map) {
        List<CustomerAuthAlreadyApplyItem> itemList = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
            CustomerAuthAlreadyApplyItem item = new CustomerAuthAlreadyApplyItem();
            item.setIcon(entry.getKey());
            item.setNodeIds(entry.getValue());
            itemList.add(item);
        }
        CustomerAuthAlreadyApplyQuery query = new CustomerAuthAlreadyApplyQuery();
        query.setItems(itemList);
        return query;
    }

}
