package com.sinohealth.system.dto.system;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-23 16:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AsyncTaskPageRequest extends PageRequest {
    private Integer status;
    private Long userId;
    private Integer flowStatus;
}
