package com.sinohealth.system.vo;

import com.sinohealth.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;



/**
 * 统计规则视图对象 mall_package
 *
 * @author dataplatform
 * @date 2021-07-30
 */
@Data
@ApiModel("统计规则视图对象")
public class SysStatisticalRulesVo {
	private static final long serialVersionUID = 1L;

	/**  */
	@ApiModelProperty("")
	private Long id;

	/** 任务名称 */
	@Excel(name = "任务名称")
	@ApiModelProperty("任务名称")
	private String jobName;
	/** 任务组名 */
	@Excel(name = "任务组名")
	@ApiModelProperty("任务组名")
	private String jobGroup;
	/** 调用目标字符串 */
	@Excel(name = "调用目标字符串")
	@ApiModelProperty("调用目标字符串")
	private String jobInvokeTarget;
	/** cron执行表达式 */
	@Excel(name = "cron执行表达式")
	@ApiModelProperty("cron执行表达式")
	private String jobCron;
	/** 计划执行错误策略（1立即执行 2执行一次 3放弃执行） */
	@Excel(name = "计划执行错误策略" , readConverterExp = "1=立即执行,2=执行一次,3=放弃执行")
	@ApiModelProperty("计划执行错误策略（1立即执行 2执行一次 3放弃执行）")
	private String jobMisfirePolicy;
	/** 是否并发执行（0允许 1禁止） */
	@Excel(name = "是否并发执行" , readConverterExp = "0=允许,1=禁止")
	@ApiModelProperty("是否并发执行（0允许 1禁止）")
	private String jobConcurrent;
	/** 状态（0正常 1暂停） */
	@Excel(name = "状态" , readConverterExp = "0=正常,1=暂停")
	@ApiModelProperty("状态（0正常 1暂停）")
	private String status;
	/** 任务执行周期类型 */
	@Excel(name = "任务执行周期类型")
	@ApiModelProperty("任务执行周期类型")
	private String statisticsPeriodType;
	/** 任务执行时间 */
	@Excel(name = "任务执行时间")
	@ApiModelProperty("任务执行时间")
	private String statisticsTime;
	/** 统计类型 */
	@Excel(name = "统计类型")
	@ApiModelProperty("统计类型")
	private String statisticsType;
	/** 统计描述 */
	@Excel(name = "统计描述")
	@ApiModelProperty("统计描述")
	private String statisticsDescribe;
	/**  */
	@Excel(name = "")
	@ApiModelProperty("")
	private Long createUserId;
	/**  */
	@Excel(name = "")
	@ApiModelProperty("")
	private Long updateUserId;

}
