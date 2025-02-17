package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.dto.AssetsComparePageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsComparePageRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 10:27
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface AssetsCompareMapper extends BaseMapper<AssetsCompare> {

    IPage<AssetsComparePageDTO> pageQueryCompare(IPage<?> page, @Param("request") AssetsComparePageRequest request);

}
