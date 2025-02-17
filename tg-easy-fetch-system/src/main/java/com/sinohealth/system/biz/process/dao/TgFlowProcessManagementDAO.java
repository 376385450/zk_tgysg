package com.sinohealth.system.biz.process.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.vo.FlowProcessSplitByTemplateVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TgFlowProcessManagementDAO extends IService<TgFlowProcessManagement> {

    /**
     * 根据类型获取最新的记录
     *
     * @param category 版本类型
     * @return 最新记录
     */
    Optional<TgFlowProcessManagement> findLatest(String category);


    /**
     * 用于全流程下属子流程分页查询补充字段
     */
    Map<Long, TgFlowProcessManagement> queryForPageList(Collection<Long> ids);

    /**
     * 根据模板id查询全流程信息
     *
     * @param templateIds 模板id
     * @return 全流程信息
     */
    List<FlowProcessSplitByTemplateVO> listByTemplateIds(List<Long> templateIds);
}
