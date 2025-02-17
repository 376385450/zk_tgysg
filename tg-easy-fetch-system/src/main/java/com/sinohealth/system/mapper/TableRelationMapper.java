package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TableRelation;
import com.sinohealth.system.dto.TableRelationDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author dataplatform
 * @date 2021-04-27
 */
public interface TableRelationMapper extends BaseMapper<TableRelation> {

    @Select({"<script>", " select t.id, b.field_name,b.field_alias,i.dir_id,f.dir_id refDirId,t.id as relationId,t.field_id,t.table_id,t.ref_field_id,t.ref_table_id,i.table_name as refTableName,i.table_alias as refTableAlias,f.field_name as refFieldName, f.field_alias as refFieldAlias from  table_relation t left join table_info i on i.id=t.ref_table_id left join table_field_info f on f.id=t.ref_field_id left join table_field_info b on b.id=t.field_id  where 1=1 ",
            "<if test=\"tableId !=null  \"> and t.table_id =#{tableId} </if>",
            "<if test=\"dirId !=null  \"> and t.dir_id =#{dirId} </if>",
            "</script>"})
    public List<TableRelationDto> getList(@Param("tableId") Long tableId, @Param("dirId") Long dirId);

}
