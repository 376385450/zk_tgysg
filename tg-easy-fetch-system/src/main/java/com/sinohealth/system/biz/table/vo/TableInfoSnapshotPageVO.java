package com.sinohealth.system.biz.table.vo;

import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-08-13 13:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TableInfoSnapshotPageVO extends TableInfoSnapshot {

    private Integer finishCnt;

    /**
     * 明细总数
     */
    private Integer detailCnt;

    private String creator;

    /**
     * 关联全流程名称
     */
    private String flowProcessName;

    private String flowProcessCategory;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    
    /**
     * 耗时
     */
    private String costTime;
}
