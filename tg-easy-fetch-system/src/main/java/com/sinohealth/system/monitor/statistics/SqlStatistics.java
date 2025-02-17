package com.sinohealth.system.monitor.statistics;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sinohealth.common.utils.spring.SpringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-13 12:39 下午
 */
public abstract class SqlStatistics<T> implements StatisticsStrategy<T> {

    private static JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);

    protected abstract String sql();

    protected ResolvableType type;

    {
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass()).getSuperType();
        ResolvableType[] types = resolvableType.getGenerics();
        this.type = types[0];
    }


    @Override
    public T getData() {
        return execute();
    }

    /**
     * 执行SQL
     * @return
     */
    private T execute() {
        Class<T> clazz = (Class<T>) type.resolve();
        if (clazz.isAssignableFrom(List.class)) {
            Class<?> resolve = type.getGenerics()[0].resolve();
            if (resolve.isAssignableFrom(Map.class)) {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql());
//                return JSON.parseObject(JSON.toJSONString(result), new TypeReference<T>(){});
                return (T) result;
            } else {
                List<?> result = jdbcTemplate.query(sql(), new BeanPropertyRowMapper<>(resolve));
                return (T) result;
            }
        } else {
            List<T> list = jdbcTemplate.queryForList(sql(), clazz);
            if (list.isEmpty()) {
                return null;
            } else {
                return list.get(0);
            }
        }
    }

}
