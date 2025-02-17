package com.sinohealth.system.biz.rangepreset.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.rangepreset.dao.RangeTemplatePresetDAO;
import com.sinohealth.system.biz.rangepreset.domain.RangeTemplatePreset;
import com.sinohealth.system.biz.rangepreset.mapper.RangeTemplatePresetMapper;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 18:11
 */
@Repository
public class RangeTemplatePresetDAOImpl
        extends ServiceImpl<RangeTemplatePresetMapper, RangeTemplatePreset>
        implements RangeTemplatePresetDAO {

}
