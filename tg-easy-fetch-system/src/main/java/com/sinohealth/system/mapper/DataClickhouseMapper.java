package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-15 19:31
 */
@Mapper
@DataSource(DataSourceType.SLAVE)
public interface DataClickhouseMapper {

    /**
     * @param tableName 表名
     * @param database  库名
     */
    @Select("SELECT `name` as fieldName,`type` as dataType,if(notEmpty(`comment`), `comment`,`fieldName`) as fieldAlias FROM system.columns WHERE database = #{database} AND `table` = #{tableName} ORDER BY position ASC")
    List<AuthTableFieldDTO> getFields(@Param("database") String database, @Param("tableName") String tableName);

    @Select("<script>${countSQL}</script>")
    Long count(@Param("countSQL") String countSQL);

    @Select("<script>${sql}</script>")
    String singleStrVal(@Param("sql") String sql);

}
