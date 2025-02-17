package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.ProjectCustomFieldDict;
import com.sinohealth.system.domain.TgApplicationInfo;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-18 19:42
 */
public interface ProjectCustomFieldDictDAO extends IService<ProjectCustomFieldDict> {

    /**
     * 保存申请中 项目和自定义列映射关系
     */
    void saveMapping(TgApplicationInfo applyInfo, String bizType);
}
