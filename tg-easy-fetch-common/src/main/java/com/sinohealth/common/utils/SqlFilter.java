package com.sinohealth.common.utils;

/**
 * @author Jingjun
 * @since 2021/5/12
 */
public class SqlFilter {
    // private static String[] keywords = "|update|exec|having|drop|delete|truncate|chardeclare|;||( )|[ ]|< >|,|.|;|:|'|\"|#|%|+|-|/|*|@|+".split("\\|");
    // 按产品要求，"-"不过滤
    private static String[] keywords = "|update|exec|having|drop|delete|truncate|chardeclare|;||( )|[ ]|< >|,|.|;|:|'|\"|#|%|+|/|*|@|+".split("\\|");

    public static String filter(String val) {
        if (!StringUtils.isEmpty(val)) {
            for (int i = 0; i < keywords.length; i++) {
                val = val.replace(keywords[i], "");
            }
        }
        return val;
    }

}
