package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.mapper.AssetsFlowBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Kuangcp
 * 2024-07-16 21:29
 */
@Slf4j
@Repository
public class AssetsFlowBatchDAOImpl extends ServiceImpl<AssetsFlowBatchMapper, AssetsFlowBatch>
        implements AssetsFlowBatchDAO {

    @Override
    public boolean updateState(Long batchId, AssetsUpgradeStateEnum state) {
        if (Objects.isNull(batchId)) {
            return false;
        }
        // 防止重复更新
        return lambdaUpdate()
                .set(AssetsFlowBatch::getState, state)
                .set(AssetsFlowBatch::getFinishTime, LocalDateTime.now())
                .eq(AssetsFlowBatch::getId, batchId)
                .ne(AssetsFlowBatch::getState, state)
                .update();
    }

    /**
     * @param batchIds
     * @param state
     */
    @Override
    public void updateState(Collection<Long> batchIds, AssetsUpgradeStateEnum state) {
        if (CollectionUtils.isEmpty(batchIds)) {
            return;
        }
        lambdaUpdate()
                .set(AssetsFlowBatch::getState, state)
                .in(AssetsFlowBatch::getId, batchIds)
                .update();

    }
}
