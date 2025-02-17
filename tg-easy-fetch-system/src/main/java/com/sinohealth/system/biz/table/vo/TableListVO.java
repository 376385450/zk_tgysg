package com.sinohealth.system.biz.table.vo;

import com.sinohealth.system.domain.TableInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-08-13 13:44
 */
@EqualsAndHashCode(callSuper = false)
@Data

public class TableListVO extends TableInfo {

    /**
     * 全流程管理中 默认展示和使用的底表
     */
    private Boolean defaultFlowProcess;
}
