package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsQcBatchDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.mapper.AssetsQcBatchMapper;
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
public class AssetsQcBatchDAOImpl
        extends ServiceImpl<AssetsQcBatchMapper, AssetsQcBatch>
        implements AssetsQcBatchDAO {

    @Override
    public void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().eq(AssetsQcBatch::getId, id)
                .set(AssetsQcBatch::getState, stateEnum.name())
                .set(AssetsQcBatch::getFinishTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updateState(Collection<Long> id, AssetsUpgradeStateEnum stateEnum) {
        if (CollectionUtils.isEmpty(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().in(AssetsQcBatch::getId, id)
                .set(AssetsQcBatch::getState, stateEnum.name())
                .update();
    }
}
