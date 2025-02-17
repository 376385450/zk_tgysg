package com.sinohealth.system.dto.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 10:02
 */
@Data
@JsonNaming
public class AuthTableFieldDTO implements Serializable {

    private Long id = 0L;

    private String table;

    @JsonProperty("filedName")
    private String fieldName;

    @JsonProperty("filedAlias")
    private String fieldAlias;

    private String dataType;

}
