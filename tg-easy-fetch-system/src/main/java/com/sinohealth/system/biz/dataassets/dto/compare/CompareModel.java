package com.sinohealth.system.biz.dataassets.dto.compare;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-15 15:50
 */
public interface CompareModel {

    void setTotal(Long total);

    Long getTotal();

    void fillQueryVal(List<LinkedHashMap<String, Object>> result);
}
