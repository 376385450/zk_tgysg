package com.sinohealth.system.dto.application.deliver.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-11-12 18:35
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class HistoryQueryRequest extends PageRequest {

    private Long projectId;

    private String applyName;

    private String projectName;

    private Integer state;

    private Long userId;
    /**
     * 内部参数
     * 项目名称查询出的结果，过滤掉关注的项目id
     */
    private List<Long> projectIds;
}
