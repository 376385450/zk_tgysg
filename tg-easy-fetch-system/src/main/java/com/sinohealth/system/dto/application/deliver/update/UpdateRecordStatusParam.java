package com.sinohealth.system.dto.application.deliver.update;

import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-10 17:08
 */
@Data
@Builder
public class UpdateRecordStatusParam {

    private Long assetId;

    private Integer version;


    /**
     * 同步目标: 1 易数阁内网CK 2 客户外网CK
     *
     * @see SyncTargetType
     */
    private Integer syncTarget;

    /**
     * 更新状态  1 待更新 2 更新中 3 成功 4 失败
     *
     * @see UpdateRecordStateType
     */
    private Integer updateState;

    private List<Integer> updateStates;
}
