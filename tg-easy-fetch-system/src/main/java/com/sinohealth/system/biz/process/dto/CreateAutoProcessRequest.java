package com.sinohealth.system.biz.process.dto;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class CreateAutoProcessRequest {
    /**
     * 期数
     */
    private String period;

    /**
     * 版本类型【qc、sop、交付、临时改数】
     */
    private FlowProcessTypeEnum type;

    /**
     * 品类编码【为空时，全表跑批】
     */
    private List<String> prodCodes;
}
