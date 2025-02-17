package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.KeyValDict;
import org.springframework.stereotype.Repository;

/**
 * @author Kuangcp
 * 2024-08-13 15:58
 */

@Repository
@DataSource(DataSourceType.MASTER)
public interface KeyValDictMapper extends BaseMapper<KeyValDict> {
}
