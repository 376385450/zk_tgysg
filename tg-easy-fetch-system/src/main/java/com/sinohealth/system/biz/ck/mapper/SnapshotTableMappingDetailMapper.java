package com.sinohealth.system.biz.ck.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMappingDetail;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-07 13:56
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface SnapshotTableMappingDetailMapper extends BaseMapper<SnapshotTableMappingDetail> {
}
