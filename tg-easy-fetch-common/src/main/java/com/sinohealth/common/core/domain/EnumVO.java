package com.sinohealth.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-13 20:19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumVO<T> {
    private T type;
    private String desc;
}
