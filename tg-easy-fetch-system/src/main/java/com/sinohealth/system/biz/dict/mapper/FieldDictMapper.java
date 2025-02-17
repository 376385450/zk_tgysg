package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:21
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface FieldDictMapper extends BaseMapper<FieldDict> {

    @Select("select max(sort) from tg_field_dict")
    Integer queryMaxSort();
}
