package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-31 14:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerCreateBO {

    private TgApplicationInfo apply;

    private TgTemplateInfo template;

    private Project project;

    private Integer workflowId;
}
