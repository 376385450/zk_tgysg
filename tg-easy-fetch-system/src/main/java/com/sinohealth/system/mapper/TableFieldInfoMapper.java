package com.sinohealth.system.mapper;

import com.sinohealth.system.domain.TableFieldInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author dataplatform
 * @date 2021-04-24
 */
@Repository
public interface TableFieldInfoMapper extends BaseMapper<TableFieldInfo> {

    List<TableFieldInfo> findListByIds(@Param("ids") List<Long> ids);

    int getCountByTableId(@Param("tableId") Long tableId,@Param("fieldName") String fieldName);

    List<TableFieldInfo> findListByFieldIds(@Param("ids") List<Long> ids);

    List<String> getFieldsByTableName(@Param("tableName") String tableName);
}
