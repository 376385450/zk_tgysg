package com.sinohealth.system.biz.dir.dto;

import com.sinohealth.system.domain.constant.ApplicationConst;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-18 15:30
 */
@Data
public class AssetsSortEditRequest {

    /**
     * 数据id
     */
    @NotNull(message = "业务id为空")
    private Long bizId;

    /**
     * 排序值 正整数
     */
    @NotNull(message = "排序值为空")
    @Min(value = 1, message = "最小为1")
    private Integer sort;
    /**
     * 模块 template, doc, table
     *
     * @see ApplicationConst.DirItemType
     */
    @NotBlank(message = "业务模块为空")
    private String module;

    /**
     * true: 插队时的二次确认
     */
    private Boolean confirm;
}
