package com.sinohealth.system.biz.dataassets.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/2/28
 */
@Data
public class ApplicationTree {

    private Long applicationId;

    private List<Long> childIds;

}
