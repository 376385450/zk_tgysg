package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushDetail;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 09:52
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface PowerBiPushDetailMapper extends BaseMapper<PowerBiPushDetail> {
}
