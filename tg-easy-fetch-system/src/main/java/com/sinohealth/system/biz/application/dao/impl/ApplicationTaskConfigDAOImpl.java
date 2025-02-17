package com.sinohealth.system.biz.application.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.mapper.ApplicationTaskConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-25 17:51
 */
@Repository
public class ApplicationTaskConfigDAOImpl extends ServiceImpl<ApplicationTaskConfigMapper, ApplicationTaskConfig>
        implements ApplicationTaskConfigDAO {
    @Override
    public ApplicationTaskConfig queryByApplicationId(Long applicationId) {
        if (Objects.isNull(applicationId)) {
            return null;
        }
        return this.baseMapper.selectOne(new QueryWrapper<ApplicationTaskConfig>().lambda().eq(ApplicationTaskConfig::getApplicationId, applicationId));
    }
}
