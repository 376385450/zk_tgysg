package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/3/26
 */
@Getter
@AllArgsConstructor
public enum AuditTypeEnum {

    // 审核通过
    PASS(1),
    // 驳回
    REJECT(2),
    // 出数成功
    SUCCESS(3),
    // 中间流程审核通过
    MIDDLE_PASS(4),
    /**
     * 出数失败
     */
    FAILED(5);

    private Integer value;


    public static List<Integer> auditType = Arrays.asList(SUCCESS.value, FAILED.value);
}
