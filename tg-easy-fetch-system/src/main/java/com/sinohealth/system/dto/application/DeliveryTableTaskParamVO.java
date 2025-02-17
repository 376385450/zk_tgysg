package com.sinohealth.system.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-23 21:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTableTaskParamVO {

    private Long assetsId;

    private Integer version;

    private Long userId;
}
