package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:51
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface BizDataDictDefineMapper extends BaseMapper<BizDataDictDefine> {
}
