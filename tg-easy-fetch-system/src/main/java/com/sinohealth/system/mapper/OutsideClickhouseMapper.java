package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 10:11
 */
@Mapper
@DataSource(DataSourceType.CUSTOMER_CK)
public interface OutsideClickhouseMapper extends BaseOutsideDatabaseMapper {

    /**
     * 查询数据表sql
     *
     * @param tableName
     * @param whereSql
     * @param querySize
     * @param offset
     * @param sortBy
     * @param sortField
     * @return
     */
    default String buildSelectDataSQL(String tableName, String whereSql, Integer querySize, Integer offset, String sortBy, String sortField) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);
        if (StringUtils.isNotBlank(whereSql)) {
            sqlBuilder.append(" WHERE ").append(whereSql);
        }
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortField)) {
            sqlBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortBy);
        }
        if (querySize != null) {
            sqlBuilder.append(" LIMIT ").append(querySize);
        }
        if (querySize != null && offset != null) {
            sqlBuilder.append(" OFFSET ").append(offset);
        }
        return sqlBuilder.toString();
    }

    /**
     * 查询数据条数sql
     *
     * @param tableName
     * @param whereSql
     * @return
     */
    default String buildCountSQL(String tableName, String whereSql) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT COUNT(*) FROM ").append(tableName);
        if (StringUtils.isNotBlank(whereSql)) {
            sqlBuilder.append(" WHERE ").append(whereSql);
        }

        return sqlBuilder.toString();
    }

    /**
     * @param tableName 表名
     * @param database  库名
     */
    @Select("SELECT `name` as fieldName,`type` as dataType,if(notEmpty(`comment`), `comment`,`fieldName`) as fieldAlias " +
            "FROM system.columns WHERE database = #{database} AND `table` = #{tableName} ORDER BY position ASC")
    List<AuthTableFieldDTO> getFields(@Param("database") String database, @Param("tableName") String tableName);

    /**
     * 查询外网ck表数据
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<LinkedHashMap<String, Object>> selectBySQL(@Param("sql") String sql);

    /**
     * 获取外网ck表数据条数
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    Long getDataCount(@Param("sql") String sql);
}
