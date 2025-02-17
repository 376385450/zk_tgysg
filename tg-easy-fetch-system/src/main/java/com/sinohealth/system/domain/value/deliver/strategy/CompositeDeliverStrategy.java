package com.sinohealth.system.domain.value.deliver.strategy;

import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DeliverStrategyFactory;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeDeliverStrategy {

    private final DeliverStrategyFactory deliverStrategyFactory;

    public List<Resource> deliver(List<DataSource> dataSources, DeliverResourceType type) throws Exception {
        if (CollectionUtils.isEmpty(dataSources)) {
            return Collections.emptyList();
        }
        // fixme 可优化为多线程，但是得保证线程上下文的正确传递
        List<Resource> resourceList = new ArrayList<>();
        for (DataSource dataSource : dataSources) {
            ResourceDeliverStrategy strategy = deliverStrategyFactory.getStrategy(dataSource, type);
            try {
                Resource resource = strategy.deliver(dataSource);
                resourceList.add(resource);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                // 如果中途错误，抛出异常
                log.error("", e);
                throw new IllegalArgumentException("文件导出失败, 请联系管理员");
            }
        }
        return resourceList;
    }


}
