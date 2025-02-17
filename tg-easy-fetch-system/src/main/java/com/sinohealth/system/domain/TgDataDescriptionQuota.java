package com.sinohealth.system.domain;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sinohealth.system.domain.constant.ApplicationConst;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:14
 */
@Data
@JsonNaming
@Accessors(chain = true)
public class TgDataDescriptionQuota {

    private String key;

    private String displayName;

    private String value;

    /**
     * @see ApplicationConst.FieldSource
     */
    private Integer fieldSource;
}
