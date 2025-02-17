package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.CustomFieldInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CustomFieldInfoMapper extends BaseMapper<CustomFieldInfo> {
    void insertOrUpdate(@Param("customFieldInfo") CustomFieldInfo customFieldInfo);

    List<CustomFieldInfo> selectCustomFields(@Param("sourceId") Long sourceId, @Param("source") Integer source);

    CustomFieldInfo queryById(@Param("id") Long id);

    CustomFieldInfo selectCustomFieldNameBySourceIdAndFieldName(@Param("sourceId") long sourceId, @Param("fieldName") String fieldName);
}
