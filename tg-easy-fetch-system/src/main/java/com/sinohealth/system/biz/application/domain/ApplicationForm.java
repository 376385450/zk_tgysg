package com.sinohealth.system.biz.application.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 需求单 抽象意义关联到多份的申请单，对应现实中的需求
 *
 * @author Kuangcp
 * 2024-12-10 13:59
 * @see ApplicationFormDAO#submitApply
 */
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "提数申请的 需求单")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_application_form")
@Accessors(chain = true)
public class ApplicationForm extends Model<ApplicationForm> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 当前关注的申请id
     */
    private Long applicationId;

    @ApiModelProperty("需求编号")
    private String applicationNo;

    /**
     * false 要处理但是没处理（一次出数）
     * true 处理过 或者 无需处理 （二次出数）
     * <p>
     * 1. 提交的申请（首次和重新申请）是否有处理过
     * 1. 验收不通过
     */
    private Boolean enterRun;

    /**
     * @see ApplyStateEnum
     */
    @ApiModelProperty("需求单状态")
    private String applyState;
    /**
     * @see ApplyRunStateEnum
     */
    @ApiModelProperty("需求单流程状态")
    private String applyRunState;

    /**
     * 需求单 待处理的那一刻保存期数的快照 从排期表来
     */
    private String period;

    private String bizType;
}
