package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.enums.dataassets.AssetsCompareTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 10:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssetsComparePageRequest extends PageRequest {

    private String searchName;

    private String bizType;

    private List<String> prodCode;

    @Deprecated
    private String tableName;

    private String period;

    private String templateName;

    /**
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    /**
     * @see AssetsCompareTypeEnum
     */
    @NotBlank(message = "类型为空")
    private String createType;

    private Date startTime;

    private Date endTime;

    // DB
    private Set<Long> projectIds;
}
