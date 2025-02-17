package com.sinohealth.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: ChenJiaRong
 * Date:   2021/7/20
 * Explain: com.sinohealth.common.enums.LogType 枚举的整合枚举
 */
@Getter
public enum LogTypeGather {


    METADATA_ALL(1099, "元数据变更", LogType.METADATA_ALL),

    RELATION_ALL(2099, "关联变更", LogType.RELATION_ALL),

    TABLE_ALL(3099, "表单变更", LogType.TABLE_ALL),

    DATA_ALL(4099, "数据变更", LogType.DATA_ALL);


    private final String name;
    private final int val;
    private final List<Integer> logTypeList;

    private LogTypeGather(int val, String name, List<Integer> logTypeList) {
        this.val = val;
        this.name = name;
        this.logTypeList = logTypeList;
    }

    public static List<Integer> findLogTypeList(int val) {
        for (LogTypeGather log : LogTypeGather.values()) {
            if (val == log.getVal()) {
                return log.getLogTypeList();
            }
        }
        return null;
    }

}
