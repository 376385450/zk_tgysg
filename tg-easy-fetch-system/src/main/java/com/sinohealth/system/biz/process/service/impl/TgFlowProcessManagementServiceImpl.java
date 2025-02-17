package com.sinohealth.system.biz.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.enums.process.FlowProcessStateEnum;
import com.sinohealth.common.enums.process.FlowProcessTaskEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.dto.FlowProcessPageRequest;
import com.sinohealth.system.biz.process.service.TgFlowProcessManagementService;
import com.sinohealth.system.biz.process.vo.FlowProcessSplitByTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TgFlowProcessManagementServiceImpl implements TgFlowProcessManagementService {
    private final TgFlowProcessManagementDAO tgFlowProcessManagementDAO;

    @Override
    public IPage<TgFlowProcessManagement> page(FlowProcessPageRequest request) {
        return tgFlowProcessManagementDAO.page(request.buildPage(), new QueryWrapper<TgFlowProcessManagement>().lambda()
            .like(StringUtils.isNotBlank(request.getName()), TgFlowProcessManagement::getName, request.getName())
            .eq(StringUtils.isNotBlank(request.getState()), TgFlowProcessManagement::getState, request.getState())
            .like(StringUtils.isNotBlank(request.getPeriod()), TgFlowProcessManagement::getPeriod, request.getPeriod())
            .eq(StringUtils.isNotBlank(request.getCategory()), TgFlowProcessManagement::getVersionCategory,
                request.getCategory())
            .like(StringUtils.isNotBlank(request.getModelAssertNames()), TgFlowProcessManagement::getTemplateNames,
                request.getModelAssertNames())
            .orderByDesc(TgFlowProcessManagement::getCreateTime));
    }

    @Override
    public TgFlowProcessManagement queryById(Long id) {
        return tgFlowProcessManagementDAO.getById(id);
    }

    @Override
    public List<TgFlowProcessManagement> queryByIds(Set<Long> ids) {
        return tgFlowProcessManagementDAO.listByIds(ids);
    }

    @Override
    public void delete(Long id) {
        tgFlowProcessManagementDAO.removeById(id);
    }

    @Override
    public void saveOrUpdate(TgFlowProcessManagement detail) {
        tgFlowProcessManagementDAO.saveOrUpdate(detail);
    }

    @Override
    public List<TgFlowProcessManagement> query(String state, Date date) {
        return tgFlowProcessManagementDAO.list(new LambdaQueryWrapper<TgFlowProcessManagement>()
            .eq(StringUtils.isNotBlank(state), TgFlowProcessManagement::getState, state)
            .le(Objects.nonNull(date), TgFlowProcessManagement::getPlanExecutionTime, date));
    }

    @Override
    public void saveOrUpdateBatch(List<TgFlowProcessManagement> details) {
        tgFlowProcessManagementDAO.saveOrUpdateBatch(details);
    }

    @Override
    public List<TgFlowProcessManagement> listByName(String name) {
        return tgFlowProcessManagementDAO.list(new LambdaQueryWrapper<TgFlowProcessManagement>()
            .eq(StringUtils.isNotBlank(name), TgFlowProcessManagement::getName, name));
    }

    @Override
    public List<TgFlowProcessManagement> queryRunningDatas(FlowProcessTaskEnum taskEnum) {
        LambdaQueryWrapper<TgFlowProcessManagement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.SYNC.equals(taskEnum),
            TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.RUNNING.getCode());
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.WORK_FLOW.equals(taskEnum),
            TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.RUNNING.getCode());
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.TABLE_DATA_COMPARE.equals(taskEnum),
            TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.RUNNING.getCode());
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.QC.equals(taskEnum),
            TgFlowProcessManagement::getQcState, FlowProcessStateEnum.RUNNING.getCode());
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.PLAN_COMPARE.equals(taskEnum),
            TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.RUNNING.getCode());
        wrapper.eq(Objects.nonNull(taskEnum) && FlowProcessTaskEnum.PUSH_POWER_BI.equals(taskEnum),
            TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.RUNNING.getCode());
        return tgFlowProcessManagementDAO.list(wrapper);
    }

    @Override
    public List<TgFlowProcessManagement> query(LambdaQueryWrapper<TgFlowProcessManagement> wrapper) {
        return tgFlowProcessManagementDAO.list(wrapper);
    }

    @Override
    public List<FlowProcessSplitByTemplateVO> listByTemplateIds(List<Long> templateIds) {
        return tgFlowProcessManagementDAO.listByTemplateIds(templateIds);
    }
}
