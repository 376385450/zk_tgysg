package com.sinohealth.system.biz.audit.dto;

import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;


/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-26 14:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditRequest extends ProcessNodeEasyDto {
    /**
     * 确认数据量
     */
    private Boolean confirmData;

    /**
     * 评估结果
     */
    @Length(max = 200, message = "评估结果超长")
    private String evaluationResult;

}
