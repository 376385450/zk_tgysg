package com.sinohealth.system.biz.application.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-10-17 19:52
 */
@Data
public class TagQueryRequest {

    private String key;
    private List<String> projectNames;
    private List<String> tags;
}
