package com.sinohealth.system.biz.dict.constant;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;

/**
 * @author Kuangcp
 * 2024-08-13 16:04
 */
public interface KeyDictType {

    String period = "flowPeriod";

    /**
     * @see FlowProcessTypeEnum
     */
    String flowProcessType = "flowProcessType";

    /**
     * 尚书台库表绑定key前缀
     */
    String syncTableKeyPrefix = "syncTable-";

    /**
     * 底表对比key前缀
     */
    String snapshotDiffKeyPrefix = "snapshotDiff-";
}
