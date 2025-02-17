package com.sinohealth.system.dto.api.cataloguemanageapi;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/9/15
 */
@Data
public class CatalogueAllDTO implements Serializable {


    private Integer id;

    private String name;

    private String desc;

    private Integer parentId;

    private List<CatalogueAllDTO> child;

}
