package com.sinohealth.system.util;

import org.springframework.data.util.Pair;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-01 17:18
 */
public class TgDataSourceUtil {

    /**
     * Clickhouse 使用的 9000 端口进行同步，所以无需指定端口
     *
     * @return host -> db
     */
    public static Pair<String, String> parseHostAndDb(String url) {
        String[] split = url.split("/");
        String host = split[2];
        String[] hosts = host.split(":");
        String db = split[3];
        String[] dbs = db.split("\\?");
        return Pair.of(hosts[0], dbs[0]);
    }
}
