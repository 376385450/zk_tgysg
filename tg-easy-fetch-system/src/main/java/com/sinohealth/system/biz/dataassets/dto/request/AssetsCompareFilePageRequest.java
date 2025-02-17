package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-17 16:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetsCompareFilePageRequest extends PageRequest {

    private String searchName;

    private List<String> prodCode;

    private String period;

    private String state;

    private Date startTime;

    private Date endTime;
}
