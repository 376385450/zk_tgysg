package com.sinohealth.system.biz.process.dao;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.process.domain.TgFlowProcessAlertConfig;

public interface TgFlowProcessAlertConfigDAO extends IService<TgFlowProcessAlertConfig> {
    /**
     * 根据类型与编码获取配置
     *
     * @param category 类型
     * @param code 编码
     * @return 告警设置
     */
    TgFlowProcessAlertConfig query(String category, String code);

    /**
     * 根据类型查询相关告警配置
     *
     * @param category 类型
     * @return 告警配置
     */
    List<TgFlowProcessAlertConfig> list(String category);
}
