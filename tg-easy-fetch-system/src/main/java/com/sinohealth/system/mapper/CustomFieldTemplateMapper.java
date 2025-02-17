package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.CustomFieldTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-30 19:29
 */
@Mapper
@Repository
public interface CustomFieldTemplateMapper extends BaseMapper<CustomFieldTemplate> {
}
