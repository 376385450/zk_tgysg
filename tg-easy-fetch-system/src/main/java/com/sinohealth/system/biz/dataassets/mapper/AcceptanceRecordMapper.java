package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:34
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface AcceptanceRecordMapper extends BaseMapper<AcceptanceRecord> {
}
