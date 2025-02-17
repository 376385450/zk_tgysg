package com.sinohealth.system.dto.system;/**
 * @author linshiye
 */

import lombok.Data;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-03-09 6:55 下午
 */
@Data
public class AsyncTaskDto {
    /** 自增id */
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 类型 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.Type}
     */
    private Integer type;

    private String paramJson;

    /**
     * 备注，异常时记录日志人工排查
     */
    private String remark;

    private Long userId;

    /**
     * 业务类型 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.BUSINESS_TYPE}
     */
    private Integer businessType;

}
