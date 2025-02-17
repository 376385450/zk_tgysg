package com.sinohealth.system.biz.table.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.table.domain.TablePushAssetsPlan;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 15:00
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface TablePushAssetsPlanMapper extends BaseMapper<TablePushAssetsPlan> {
}
