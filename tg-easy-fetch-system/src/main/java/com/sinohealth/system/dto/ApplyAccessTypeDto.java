package com.sinohealth.system.dto;

import com.sinohealth.common.enums.AccessType;
import lombok.Data;

/**
 * @author Jingjun
 * @since 2021/6/21
 */
@Data
public class ApplyAccessTypeDto {

    private Integer applyId;
    private int accessType;

    public String getAccessTypeName() {
        return AccessType.getAccessTypeName(accessType);
    }

}
