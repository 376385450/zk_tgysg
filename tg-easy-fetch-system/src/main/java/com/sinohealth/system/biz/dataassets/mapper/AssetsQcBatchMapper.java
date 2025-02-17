package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushPageRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 09:52
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface AssetsQcBatchMapper extends BaseMapper<AssetsQcBatch> {

    IPage<PowerBiPushBatch> pageQuery(IPage<?> page, @Param("request") PowerBiPushPageRequest request);
}
