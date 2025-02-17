package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import com.sinohealth.system.biz.dataassets.mapper.AssetsFlowBatchDetailMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-07-17 10:19
 */
@Repository
public class AssetsFlowBatchDetailDAOImpl
        extends ServiceImpl<AssetsFlowBatchDetailMapper, AssetsFlowBatchDetail>
        implements AssetsFlowBatchDetailDAO {

    @Override
    public Optional<Long> queryBatchId(Long detailId) {
        return lambdaQuery()
                .eq(AssetsFlowBatchDetail::getId, detailId)
                .oneOpt().map(AssetsFlowBatchDetail::getBatchId);
    }

    public List<AssetsFlowBatchDetail> queryByBatchId(Long batchId) {
        if (Objects.isNull(batchId)) {
            return Collections.emptyList();
        }

        return lambdaQuery()
                .eq(AssetsFlowBatchDetail::getBatchId, batchId)
                .list();
    }

    @Override
    public List<AssetsFlowBatchDetail> queryByBatchId(Collection<Long> batchIds) {
        if (CollectionUtils.isEmpty(batchIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(AssetsFlowBatchDetail::getBatchId, batchIds)
                .list();
    }

    @Override
    public void updateState(Long detailId, AssetsUpgradeStateEnum state) {
        if (Objects.isNull(detailId)) {
            return;
        }
        this.updateState(Collections.singleton(detailId), state);
    }

    @Override
    public void updateState(Collection<Long> detailIds, AssetsUpgradeStateEnum state) {
        if (CollectionUtils.isEmpty(detailIds) || Objects.isNull(state)) {
            return;
        }

        boolean end = state.isEnd();
        LocalDateTime now = LocalDateTime.now();
        this.baseMapper.update(null, new UpdateWrapper<AssetsFlowBatchDetail>().lambda()
                .in(AssetsFlowBatchDetail::getId, detailIds)
                .set(end, AssetsFlowBatchDetail::getFinishTime, now)
                .set(!end, AssetsFlowBatchDetail::getStartTime, now)
                .set(AssetsFlowBatchDetail::getState, state)
        );
    }
}
