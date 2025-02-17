package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsQcDetailDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcDetail;
import com.sinohealth.system.biz.dataassets.mapper.AssetsQcDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:18
 */
@Slf4j
@Repository
public class AssetsQcDetailDAOImpl
        extends ServiceImpl<AssetsQcDetailMapper, AssetsQcDetail>
        implements AssetsQcDetailDAO {

    @Override
    public void updateStartState(Long id) {
        if (Objects.isNull(id)) {
            log.warn("update failed");
            return;
        }

        lambdaUpdate().eq(AssetsQcDetail::getId, id)
                .set(AssetsQcDetail::getState, AssetsUpgradeStateEnum.running.name())
                .set(AssetsQcDetail::getStartTime, LocalDateTime.now())
                .update();
    }


    @Override
    public void updateFinishState(Long id, AssetsUpgradeStateEnum stateEnum, String runLog) {
        if (Objects.isNull(id)) {
            log.warn("update failed: stateEnum={}", stateEnum);
            return;
        }

        lambdaUpdate()
                .eq(AssetsQcDetail::getId, id)
                .set(AssetsQcDetail::getState, stateEnum.name())
                .set(AssetsQcDetail::getRunLog, runLog)
                .set(AssetsQcDetail::getFinishTime, LocalDateTime.now())
                .update();
    }
}
