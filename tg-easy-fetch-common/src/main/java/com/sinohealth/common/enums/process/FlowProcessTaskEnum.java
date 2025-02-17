package com.sinohealth.common.enums.process;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum FlowProcessTaskEnum {
	/**
	 * 宽表同步
	 */
	SYNC("sync", "宽表同步"),

	/**
	 * 工作流推数
	 */
	WORK_FLOW("workFlow", "工作流推数"),

	/**
	 * 库表比对
	 */
	TABLE_DATA_COMPARE("tableDataCompare", "库表比对"),

	/**
	 * 项目qc
	 */
	QC("qc", "项目qc"),

	/**
	 * 数据对比
	 */
	PLAN_COMPARE("planCompare", "数据对比"),

	/**
	 * powerBi推数
	 */
	PUSH_POWER_BI("pushPowerBi", "powerBi推数"),

	/**
	 * 资产升级
	 */
	ASSETS_UPDATE("assetsUpdate", "资产升级"),;

	/**
	 * 任务编码
	 */
	private final String code;

	/**
	 * 描述
	 */
	private final String desc;

	FlowProcessTaskEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	/**
	 * 判断是否匹配
	 *
	 * @param code 编码
	 * @return 是否匹配
	 */
	public boolean match(String code) {
		for (FlowProcessTaskEnum value : FlowProcessTaskEnum.values()) {
			if (Objects.equals(value.getCode(), code)) {
				return true;
			}
		}
		return false;
	}
}
