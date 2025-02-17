package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.system.biz.application.dto.request.ColumnSetting;
import com.sinohealth.system.domain.ApplicationColumnSetting;
import com.sinohealth.system.mapper.ApplicationColumnSettingMapper;
import com.sinohealth.system.service.ApplicationColumnSettingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/12/28
 */
@Service
public class ApplicationColumnSettingServiceImpl implements ApplicationColumnSettingService {

    @Autowired
    ApplicationColumnSettingMapper applicationColumnSettingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveColumnSetting(List<ColumnSetting> columns, Long applicationId) {
        // 删除原有列设置
        final LambdaQueryWrapper<ApplicationColumnSetting> wq = Wrappers.<ApplicationColumnSetting>lambdaQuery()
                .eq(ApplicationColumnSetting::getApplicationId, applicationId);
        applicationColumnSettingMapper.delete(wq);

        if (CollUtil.isNotEmpty(columns)) {
            for (ColumnSetting column : columns) {
                final ApplicationColumnSetting setting = new ApplicationColumnSetting();
                BeanUtils.copyProperties(column, setting);
                setting.setApplicationId(applicationId);
                setting.setId(null);
                applicationColumnSettingMapper.insert(setting);
            }
        }

    }

}
