package com.sinohealth.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcOperations;

import javax.sql.DataSource;

/**
 * @author Jingjun
 * @since 2021/4/26
 */
@Getter
@AllArgsConstructor
public class DataConnection {

    private String schema;

    private DataSource dataSource;

    private JdbcOperations jdbcOperations;

    /**
     * 数据源对应数据库类型（对应 feature 字段）
     * 详情见 {@link com.sinohealth.bi.enums.DatabaseEnum}
     */
    private String databaseType;

}
