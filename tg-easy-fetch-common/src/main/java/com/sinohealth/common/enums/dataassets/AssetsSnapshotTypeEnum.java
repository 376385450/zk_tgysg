package com.sinohealth.common.enums.dataassets;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-31 17:53
 */
public enum AssetsSnapshotTypeEnum {
    /**
     * 定时触发
     */
    schedule_deliver,
    /**
     * 审核方 手动触发
     */
    manual_deliver,
    /**
     * 底表同步 触发
     */
    sync_deliver,

    /**
     * 申请人 手动执行触发
     */
    apply_deliver,

    /**
     * 重新申请
     */
    re_apply,
    /**
     * 需求申请
     */
    apply,

    ;

    public static final Set<String> AUTO_DELETE_SCOPE = Sets.newHashSet(schedule_deliver.name(), sync_deliver.name(),
            apply_deliver.name(), manual_deliver.name());

    public static final Map<String, String> snapshotMap = new HashMap<String, String>() {{
        put("schedule_deliver", "定时触发");
        put("manual_deliver", "手动触发");
        put("sync_deliver", "底表同步触发");
        put("re_apply", "重新申请");
        put("apply", "需求申请");
    }};
}
