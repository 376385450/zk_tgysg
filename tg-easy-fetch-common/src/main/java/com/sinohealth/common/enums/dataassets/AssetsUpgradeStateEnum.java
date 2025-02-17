package com.sinohealth.common.enums.dataassets;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import com.sinohealth.common.enums.ExecutionStatus;

/**
 * 资产升级任务，资产对比任务
 *
 * @author kuangchengping@sinohealth.cn 2023-08-23 18:05
 */
public enum AssetsUpgradeStateEnum {

    wait, running, success, failed;

    public static Set<String> actions = Sets.newHashSet(wait.name(), running.name());
    public static Set<String> end = Sets.newHashSet(success.name(), failed.name());

    public static AssetsUpgradeStateEnum ofFlowState(Integer state) {
        if (ExecutionStatus.SUCCEED.contains(state)) {
            return AssetsUpgradeStateEnum.success;
        } else {
            return AssetsUpgradeStateEnum.failed;
        }
    }

    public boolean isEnd() {
        return Objects.equals(success, this) || Objects.equals(failed, this);
    }

    public static AssetsUpgradeStateEnum match(String name) {
        for (AssetsUpgradeStateEnum value : AssetsUpgradeStateEnum.values()) {
            if (Objects.equals(value.name(), name)) {
                return value;
            }
        }
        return null;
    }
}
