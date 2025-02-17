package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.mapper.AssetsWideUpgradeTriggerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:53
 */
@Slf4j
@Repository
public class AssetsWideUpgradeTriggerDAOImpl
        extends ServiceImpl<AssetsWideUpgradeTriggerMapper, AssetsWideUpgradeTrigger>
        implements AssetsWideUpgradeTriggerDAO {

    @Override
    public boolean queryNeedTableIds(Long tableId, String endTime) {
        // null 标识没有超过这个时间的任务创建，需要创建任务
        Long overTime = baseMapper.queryNeedTableIds(tableId, endTime);
        return Objects.isNull(overTime);
    }
}
