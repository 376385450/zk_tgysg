package com.sinohealth.system.biz.dataassets.dto.bo;

import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.dataassets.dto.ProcessDefStatusDTO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-26 16:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyFlowContext {

    private ProcessDefStatusDTO statusDTO;
    private TgTemplateInfo template;
    private ApplicationTaskConfig config;
    private TgApplicationInfo apply;
    private Integer flowId;

}
