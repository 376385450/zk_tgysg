package com.sinohealth.system.biz.process.vo;

import lombok.Data;

@Data
public class FlowProcessSplitByTemplateVO {
    /**
     * 全流程id
     */
    private Long id;

    /**
     * 全流程名称
     */
    private String name;

    /**
     * 模板id
     */
    private Long templateId;
}
