package com.sinohealth.system.biz.dataassets.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/1/12
 */
@Data
public class MyAssetRequest {

    private String applicationName;

    private String requireAttr;

    private String bizType;

    private Long userId;

    private Integer pageNum;

    private Integer pageSize;

    private List<Long> userDataAssetIds;

    private List<Long> excludeAssetIds;

    private Date nowTime;

}
