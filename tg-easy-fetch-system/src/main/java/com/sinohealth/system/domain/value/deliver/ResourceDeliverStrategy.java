package com.sinohealth.system.domain.value.deliver;


import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.dto.ApplicationDataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-25 13:45
 */
public interface ResourceDeliverStrategy<T extends DataSource, R extends Resource> {

    /**
     * 分配资源
     *
     * @param dataSource 数据来源
     * @return 目标资源
     */
    R deliver(T dataSource) throws Exception;

    default String[] getHeaders(ApplicationDataDto applicationDataFromCk, Map<Long, String> aliasMap) {
        Map<String, List<String>> headerCount = applicationDataFromCk.getHeader().stream()
                .map(a -> StringUtils.isNotBlank(a.getFiledAlias()) ? a.getFiledAlias() : a.getFiledName() + "(header)")
                .collect(Collectors.groupingBy(v -> v));
        return applicationDataFromCk.getHeader().stream()
                .map(a -> {
                    String customName = a.getCustomName();
                    String alias = a.getFiledAlias();
                    String field = a.getFiledName();

                    String name = StrUtil.firstNotBlankStr(customName, alias, field + "(header)");
                    if (CollectionUtils.size(headerCount.get(name)) > 1) {
                        return aliasMap.get(a.getTableId()) + "." + name;
                    }
                    return name;
                })
                .filter(HiddenFieldUtils.APPLY_PREDICATE)
                .toArray(String[]::new);
    }
}
