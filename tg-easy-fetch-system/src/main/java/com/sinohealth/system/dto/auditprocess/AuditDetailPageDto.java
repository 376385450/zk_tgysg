package com.sinohealth.system.dto.auditprocess;

import com.sinohealth.system.domain.TgApplicationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-23 18:00
 * @Desc
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditDetailPageDto {
    private TgApplicationInfo applicationInfo;
    private List<ProcessNodeEasyDto> processNodeEasyDtos;
}
