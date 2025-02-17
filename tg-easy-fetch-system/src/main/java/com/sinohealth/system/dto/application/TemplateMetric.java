package com.sinohealth.system.dto.application;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 申请时展示模板中配置的指标信息，存储展示情况（置于Select）
 *
 * @author kuangchengping@sinohealth.cn
 * 2022-11-16 17:00
 */
@Data
public class TemplateMetric implements Serializable {

    /**
     * 别名
     */
    private List<RealName> realName;

    /**
     * 已选择的数据
     */
    private List<Long> select;

    /**
     * 全集候选数据
     */
    private List<Long> copySelect;

    /**
     * 是否全选 前端组件
     */
    private Boolean isAllState;
}
