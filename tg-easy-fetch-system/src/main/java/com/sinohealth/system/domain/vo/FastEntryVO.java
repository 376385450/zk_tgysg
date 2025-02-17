package com.sinohealth.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-23 13:36
 */
@Data
public class FastEntryVO implements Serializable {

    private Long id;
    @JsonIgnore
    private Integer sort;
    private String name;
    private String menuName;
    private String icon;
}
