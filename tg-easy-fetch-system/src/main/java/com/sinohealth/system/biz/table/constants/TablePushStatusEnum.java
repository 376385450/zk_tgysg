package com.sinohealth.system.biz.table.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:41
 */
public enum TablePushStatusEnum {
    none,
    run,
    success,
    failed;

    public static List<String> CANT_REPEAT_RUN = Arrays.asList(run.name(), success.name(), failed.name());

    public static List<String> END = Arrays.asList(success.name(), failed.name());
}
