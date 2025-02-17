package com.sinohealth.system.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.dto.request.ColumnSetting;
import com.sinohealth.system.dao.ApplicationColumnSettingDAO;
import com.sinohealth.system.domain.ApplicationColumnSetting;
import com.sinohealth.system.mapper.ApplicationColumnSettingMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2023/12/29
 */
@Repository
public class ApplicationColumnSettingDAOImpl
        extends ServiceImpl<ApplicationColumnSettingMapper, ApplicationColumnSetting>
        implements ApplicationColumnSettingDAO {


    @Override
    public List<ColumnSetting> getByApplicationId(Long applicationId) {
        final LambdaQueryWrapper<ApplicationColumnSetting> wq = Wrappers.<ApplicationColumnSetting>lambdaQuery()
                .eq(ApplicationColumnSetting::getApplicationId, applicationId);

        final List<ApplicationColumnSetting> settings = baseMapper.selectList(wq);

        if (CollUtil.isNotEmpty(settings)) {
            return settings.stream()
                    .map(a->{
                        final ColumnSetting columnSetting = new ColumnSetting();
                        BeanUtils.copyProperties(a, columnSetting);
                        return columnSetting;
                    }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
