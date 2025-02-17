package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.PowerBiPushDetailDAO;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushDetail;
import com.sinohealth.system.biz.dataassets.mapper.PowerBiPushDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:18
 */
@Slf4j
@Repository
public class PowerBiPushDetailDAOImpl
        extends ServiceImpl<PowerBiPushDetailMapper, PowerBiPushDetail>
        implements PowerBiPushDetailDAO {

    /**
     *
     */
    @Override
    public Map<Long, List<PowerBiPushDetail>> queryStateDetailByBatch(Collection<Long> batchIds) {
        if (CollectionUtils.isEmpty(batchIds)) {
            return Collections.emptyMap();
        }

        List<PowerBiPushDetail> details = lambdaQuery()
                .select(PowerBiPushDetail::getId, PowerBiPushDetail::getBatchId, PowerBiPushDetail::getState,
                        PowerBiPushDetail::getStartTime, PowerBiPushDetail::getFinishTime)
                .in(PowerBiPushDetail::getBatchId, batchIds)
                .list();

        return details.stream().collect(Collectors.groupingBy(PowerBiPushDetail::getBatchId));
    }

    @Override
    public void updateStartState(Long id) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().eq(PowerBiPushDetail::getId, id)
                .set(PowerBiPushDetail::getState, AssetsUpgradeStateEnum.running.name())
                .set(PowerBiPushDetail::getStartTime, LocalDateTime.now())
                .update();
    }


    @Override
    public void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum, String runLog) {
        if (Objects.isNull(id)) {
            log.warn("update failed: stateEnum={}", stateEnum);
            return;
        }

        lambdaUpdate()
                .eq(PowerBiPushDetail::getId, id)
                .set(PowerBiPushDetail::getState, stateEnum.name())
                .set(PowerBiPushDetail::getRunLog, runLog)
                .set(PowerBiPushDetail::getFinishTime, LocalDateTime.now())
                .update();
    }
}
