package com.sinohealth.system.biz.rangepreset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.rangepreset.domain.RangeTemplatePreset;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 18:10
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface RangeTemplatePresetMapper extends BaseMapper<RangeTemplatePreset> {
}
