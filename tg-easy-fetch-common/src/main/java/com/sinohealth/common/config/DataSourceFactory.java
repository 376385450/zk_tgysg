package com.sinohealth.common.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sinohealth.common.core.domain.entity.DataDir;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jingjun
 * @since 2021/4/25
 */
@Slf4j
public class DataSourceFactory {

    private static final Map<Integer, DataConnection> dataSourceMap = new ConcurrentHashMap<>();
    private static final Map<Long, DataDir> dirMap = new ConcurrentHashMap<>();

    public static DataConnection getDataConnection(Long dirId) {
        return dataSourceMap.get(dirMap.get(dirId).getDatasourceId());
    }


    public static void addDataSource(Integer dataSourceId, String url, String username, String password,
                                     String driverClassName, String databaseType, String sourceName)  {

        ComboPooledDataSource druidDataSource=new ComboPooledDataSource();
        druidDataSource.setUser(username);
        druidDataSource.setPassword(password);
        druidDataSource.setJdbcUrl(url);
        druidDataSource.setMinPoolSize(5);
        druidDataSource.setMaxPoolSize(30);
        druidDataSource.setCheckoutTimeout(20000);
        druidDataSource.setMaxStatements(1000);
        druidDataSource.setMaxStatements(0);
        druidDataSource.setAcquireIncrement(2);
        druidDataSource.setBreakAfterAcquireFailure(false);
        druidDataSource.setTestConnectionOnCheckout(false);
        druidDataSource.setIdleConnectionTestPeriod(60);
        try {
            druidDataSource.setDriverClass(driverClassName);
        } catch (PropertyVetoException e) {
            log.error("", e);
            throw new RuntimeException("创建连接池失败！");
        }

//        addDataSource(dataSourceId, druidDataSource, url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")),
//                databaseType);
        addDataSource(dataSourceId, druidDataSource, sourceName, databaseType);

    }

    public static void addDataSource(Integer dataSourceId, DataSource dataSource, String schema, String databaseType) {
        dataSourceMap.put(dataSourceId, new DataConnection(schema, dataSource, new JdbcTemplate(dataSource),
                databaseType));
    }


    public static void addDir(Long dirId, DataDir dir) {
        dirMap.put(dirId, dir);
    }

    public static DataDir getDir(Long dirId) {
        return dirMap.get(dirId);
    }

}
