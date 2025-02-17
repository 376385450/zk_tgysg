package com.sinohealth.system.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-19 17:47
 */
@Data
@Accessors(chain = true)
public class ArkbiAnalysisQuery {

    private List<String> assetsIds;

    private String type;

    private Boolean parent;

}
