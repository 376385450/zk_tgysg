package com.sinohealth.system.biz.process.vo;

import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingBase;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class FlowProcessAttachVO {
    private TgFlowProcessSettingBase base;
    private TgFlowProcessSettingDetail detail;
    private List<String> prodCodes;
}
