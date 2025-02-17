package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-02-22 15:14
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class UserDataAssetsDistRequest extends PageRequest {

    @ApiModelProperty("申请id")
    private Long id;

    private String search;

    private List<String> bizType;

    /**
     * @see RequireAttrType
     */
    @ApiModelProperty("需求性质 合同 销前")
    private List<Integer> requireAttr;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    private Integer requireTimeType;

    private Date startTime;

    private Date endTime;

    /**
     * 时间颗粒度
     */
    private List<String> timeGra;
    /**
     * @see ApplyStateEnum
     */
    @ApiModelProperty("需求单状态")
    private List<String> applyState;
    /**
     * @see ApplyRunStateEnum
     */
    @ApiModelProperty("需求单流程状态")
    private List<String> applyRunState;

}
