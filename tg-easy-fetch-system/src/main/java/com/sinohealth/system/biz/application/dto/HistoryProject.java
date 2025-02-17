package com.sinohealth.system.biz.application.dto;

import com.sinohealth.common.annotation.Excel;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-07 21:18
 */
@Data
public class HistoryProject {
    @Excel(name = "row")
    private String id;
    @Excel(name = "需求名称")
    private String name;
    /**
     * 主条件
     */
    @Excel(name = "产品范围")
    private String projectCondition;

    @Excel(name = "产品范围-需剔除部分")
    private String excludeCondition;

    /**
     * @see FilterDTO
     */
    @Excel(name = "filter")
    private String filter;
}
