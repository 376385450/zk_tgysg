package com.sinohealth.common.utils.poi.easyexcel;

import java.util.HashMap;
import java.util.Map;
/**
 * @author Huangzk
 * @date 2021/8/24 15:59
 */
public class SqlTypeContext {

    public static final String TABLE_ID = "tableId_202108251436";

    protected static ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    public static void put(String key, Object value) {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<>();
            context.set(map);
        }
        map.put(key, value);
    }

    public static Object get(String key) {
        Map<String, Object> map = context.get();
        if (map == null) return null;
        return map.get(key);
    }

    public static void clear() {
        context.remove();
    }

}

