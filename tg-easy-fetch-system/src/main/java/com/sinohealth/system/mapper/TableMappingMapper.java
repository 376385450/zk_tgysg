package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TableMapping;
import com.sinohealth.system.dto.TableMappingDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表字段映射信息Mapper
 *
 * @author linkaiwei
 * @date 2021/11/04 10:44
 * @since 1.6.1.0
 */
public interface TableMappingMapper extends BaseMapper<TableMapping> {

    @Select({"<script>",
            "SELECT t.id, t.table_id tableId, t.field_id fieldId, t2.field_name fieldName, t2.field_alias fieldAlias",
            ", t.relation_dir_id relationDirId, t.relation_table_id relationTableId, t4.table_name relationTableName, t4.table_alias relationTableAlias",
            ", t.relation_field_id relationFieldId, t5.field_name relationFieldName, t5.field_alias relationFieldAlias",
            ", t.mapping_field_id mappingFieldId, t6.field_name mappingFieldName, t6.field_alias mappingFieldAlias, t6.data_type, t6.primary_key",
            " FROM table_mapping t",
            " LEFT JOIN table_field_info t2 on t2.id = t.field_id",
            " LEFT JOIN table_info t4 on t4.id = t.relation_table_id",
            " LEFT JOIN table_field_info t5 on t5.id = t.relation_field_id",
            " LEFT JOIN table_field_info t6 on t6.id = t.mapping_field_id",
            " WHERE t.status != 0 AND t.table_id = #{tableId}",
            " <if test=\"fieldId != null\"> AND t.field_id = #{fieldId} </if>",
            "</script>"})
    List<TableMappingDTO> listMapping(@Param("tableId") Long tableId, @Param("fieldId") Long fieldId);

}
