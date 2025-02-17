package com.sinohealth.system.domain.value.deliver;

import cn.hutool.core.util.ClassUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 17:18
 */
@Slf4j
@Component
public class DeliverStrategyFactory implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Getter
    private Map<Class<? extends DataSource>, Map<Class<? extends Resource>, ResourceDeliverStrategy>> strategyMap;

    @Getter
    private Map<DeliverResourceType, Class<? extends Resource>> deliverTypeResourceMap;


    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategyMap();
        initDeliverTypeResourceMap();
    }

    private void initStrategyMap() {
        Map<String, ResourceDeliverStrategy> beanMap = applicationContext.getBeansOfType(ResourceDeliverStrategy.class);
        Collection<ResourceDeliverStrategy> strategies = beanMap.values();
        Map<Class<? extends DataSource>, Map<Class<? extends Resource>, ResourceDeliverStrategy>> map = new HashMap<>();
        if (!strategies.isEmpty()) {
            for (ResourceDeliverStrategy strategy : strategies) {
                Type[] genericInterfaces = strategy.getClass().getGenericInterfaces();
                Type[] actualTypeArguments;
                // 接口泛型 和 父类泛型
                if (genericInterfaces.length == 0) {
                    actualTypeArguments = ((ParameterizedType) strategy.getClass().getGenericSuperclass()).getActualTypeArguments();
                } else {
                    actualTypeArguments = ((ParameterizedType) genericInterfaces[0]).getActualTypeArguments();
                }

                Class dataSourceClass = (Class) actualTypeArguments[0];
                Class resourceClass = (Class) actualTypeArguments[1];

                map.putIfAbsent(dataSourceClass, new HashMap<>());
                map.get(dataSourceClass).put(resourceClass, strategy);
            }
        }
        strategyMap = Collections.unmodifiableMap(map);
    }

    private void initDeliverTypeResourceMap() {
        Map<DeliverResourceType, Class<? extends Resource>> map = new HashMap<>();
        // 扫描包下的Resource类
        Set<Class<?>> resourceClasses = ClassUtil.scanPackage("com.sinohealth.system.domain.value.deliver.resource", clazz -> Resource.class.isAssignableFrom(clazz));
        for (Class<?> clazz : resourceClasses) {
            boolean isAbstractClazz = Modifier.isAbstract(clazz.getModifiers());
            if (!isAbstractClazz && clazz.isAnnotationPresent(SupportResourceType.class)) {
                SupportResourceType annotation = clazz.getAnnotation(SupportResourceType.class);
                DeliverResourceType deliverResourceType = annotation.value();
                map.put(deliverResourceType, (Class<? extends Resource>) clazz);
            }
        }
        deliverTypeResourceMap = Collections.unmodifiableMap(map);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ResourceDeliverStrategy getStrategy(DataSource dataSource, DeliverResourceType type) {
        if (!strategyMap.containsKey(dataSource.getClass())) {
            throw new RuntimeException("不支持的交付数据源类型" + dataSource.support());
        }
        if (!deliverTypeResourceMap.containsKey(type)) {
            throw new RuntimeException("不支持的交付类型" + type);
        }
        ResourceDeliverStrategy resourceDeliverStrategy = strategyMap.get(dataSource.getClass()).get(deliverTypeResourceMap.get(type));
        if (resourceDeliverStrategy == null) {
            throw new RuntimeException("不支持的交付类型" + type);
        }
        return resourceDeliverStrategy;
    }
}
