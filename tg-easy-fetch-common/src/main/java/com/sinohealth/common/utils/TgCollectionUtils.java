package com.sinohealth.common.utils;

import java.util.*;

/**
 * @Author Rudolph
 * @Date 2022-06-30 9:34
 * @Desc
 */

public class TgCollectionUtils {
    public static <T> List<T> newArrayList() {
        return new ArrayList<T>(64);
    }


    public static <K, V> void appendVal(Map<K, List<V>> map, K key, V val) {
        List<V> list = map.get(key);
        if (Objects.isNull(list)) {
            ArrayList<V> cache = new ArrayList<>();
            cache.add(val);
            map.put(key, cache);
        } else {
            list.add(val);
        }
    }

    public static <K, V> void appendSetVal(Map<K, Set<V>> map, K key, V val) {
        Set<V> list = map.get(key);
        if (Objects.isNull(list)) {
            Set<V> cache = new HashSet<>();
            cache.add(val);
            map.put(key, cache);
        } else {
            list.add(val);
        }
    }
}
