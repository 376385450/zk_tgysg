package com.sinohealth.system.mapper;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author monster
 * @Date 2024-07-10 11:37
 * @Desc hive语句处理类
 */
@Slf4j
public class TgHiveProvider {

    public static String runSql(String sql) {
        return sql;
    }

    public static String countTable(String tableName) {
        return new SQL() {{
            this.SELECT("COUNT(*)").FROM(tableName);
        }}.toString();
    }

    public static String selectAllDataFromHive(String tableName) {
        return new SQL() {{
            SELECT("*")
                    .FROM(tableName);
        }}.toString();
    }

    public static String selectApplicationDataFromHive(final String sql, final String whereSql, final GetDataInfoRequestDTO requestDTO) {
        return new SQL() {{
            this.SELECT("*").FROM("( " + sql + " ) t");
            if (StringUtils.isNotBlank(whereSql)) {
                this.WHERE(whereSql);
            }
            if (StringUtils.isNoneBlank(requestDTO.getSortingField()) && StringUtils.isNoneBlank(requestDTO.getSortBy())) {
                this.ORDER_BY("`" + requestDTO.getSortingField() + "` " + requestDTO.getSortBy());
            }
            this.LIMIT(requestDTO.getPageSize());
            this.OFFSET((long) (requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
        }}.toString();
    }
}
