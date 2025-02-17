package com.sinohealth.system.biz.process.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.enums.process.FlowProcessTaskEnum;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.dto.FlowProcessPageRequest;
import com.sinohealth.system.biz.process.vo.FlowProcessSplitByTemplateVO;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface TgFlowProcessManagementService {
    /**
     * 分页
     *
     * @param request 参数
     * @return 全流程信息
     */
    IPage<TgFlowProcessManagement> page(FlowProcessPageRequest request);

    /**
     * 根据唯一标识查询
     *
     * @param id 唯一标识
     * @return 管理记录
     */
    TgFlowProcessManagement queryById(Long id);

    /**
     * 批量查询管理任务信息
     *
     * @param ids 主键
     * @return 管理任务信息
     */
    List<TgFlowProcessManagement> queryByIds(Set<Long> ids);

    /**
     * 根据id删除
     *
     * @param id 唯一标识
     */
    void delete(Long id);

    /**
     * 新增或更新
     *
     * @param detail 详情信息
     */
    void saveOrUpdate(TgFlowProcessManagement detail);

    /**
     * 批量查询
     *
     * @param state 数据状态
     * @param date  日期【限制】
     * @return 全流程管理任务信息
     */
    List<TgFlowProcessManagement> query(String state, Date date);

    /**
     * 批量更新
     *
     * @param details entitys
     */
    void saveOrUpdateBatch(List<TgFlowProcessManagement> details);

    /**
     * 根据名称获取数据
     *
     * @param name 名称
     * @return 全流程记录
     */
    List<TgFlowProcessManagement> listByName(String name);

    /**
     * 查询对应运行状态的数据
     *
     * @param taskEnum 任务类型
     * @return 对应数据
     */
    List<TgFlowProcessManagement> queryRunningDatas(FlowProcessTaskEnum taskEnum);

    /**
     * 自定义查询
     *
     * @param wrapper 查询封装
     * @return 全流程信息
     */
    List<TgFlowProcessManagement> query(LambdaQueryWrapper<TgFlowProcessManagement> wrapper);

    /**
     * 根据模板id查询
     *
     * @param templateIds 模板id
     * @return 全流程信息
     */
    List<FlowProcessSplitByTemplateVO> listByTemplateIds(List<Long> templateIds);
}
