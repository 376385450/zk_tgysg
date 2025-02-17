package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-08-09 15:40
 */
@Data
public class AssetsFlowBatchInfoDTO extends AssetsFlowBatch {

    private List<Long> applyIds;

}
