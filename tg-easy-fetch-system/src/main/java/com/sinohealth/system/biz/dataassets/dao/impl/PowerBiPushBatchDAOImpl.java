package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.PowerBiPushBatchDAO;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.dataassets.mapper.PowerBiPushBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:21
 */
@Slf4j
@Repository
public class PowerBiPushBatchDAOImpl
        extends ServiceImpl<PowerBiPushBatchMapper, PowerBiPushBatch>
        implements PowerBiPushBatchDAO {

    @Override
    public void updateState(Long id, AssetsUpgradeStateEnum stateEnum) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().eq(PowerBiPushBatch::getId, id)
                .set(PowerBiPushBatch::getState, stateEnum.name())
                .update();
    }

    @Override
    public boolean updateState(Long id, LocalDateTime finishTime, AssetsUpgradeStateEnum stateEnum) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return false;
        }

        return lambdaUpdate().eq(PowerBiPushBatch::getId, id)
                .ne(PowerBiPushBatch::getState, stateEnum.name())
                .set(PowerBiPushBatch::getState, stateEnum.name())
                .set(PowerBiPushBatch::getFinishTime, finishTime)
                .update();
    }

    @Override
    public void updateState(Collection<Long> id, AssetsUpgradeStateEnum stateEnum) {
        if (CollectionUtils.isEmpty(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().in(PowerBiPushBatch::getId, id)
                .set(PowerBiPushBatch::getState, stateEnum.name())
                .update();
    }

    /**
     * @param id
     */
    @Override
    public void logicDelete(Long id) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().eq(PowerBiPushBatch::getId, id)
                .set(PowerBiPushBatch::getDeleted, true)
                .update();
    }
}
