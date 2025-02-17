package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-20 13:52
 */
@Data
@JsonNaming
public class CustomerAuthListQuery implements Serializable {

    private Long applyId;

    private List<Long> ids;

    private Long userId;

}
