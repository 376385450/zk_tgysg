package com.sinohealth.system.biz.process.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.process.FlowProcessStateEnum;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.mapper.TgFlowProcessManagementMapper;
import com.sinohealth.system.biz.process.vo.FlowProcessSplitByTemplateVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TgFlowProcessManagementDAOImpl extends ServiceImpl<TgFlowProcessManagementMapper, TgFlowProcessManagement>
        implements TgFlowProcessManagementDAO {

    public Optional<TgFlowProcessManagement> findLatest(String category) {
        return lambdaQuery().eq(TgFlowProcessManagement::getVersionCategory, category)
                .eq(TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.SUCCESS.getCode())
                .orderByDesc(TgFlowProcessManagement::getCreateTime).last(" limit 1").oneOpt();
    }

    public Map<Long, TgFlowProcessManagement> queryForPageList(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<TgFlowProcessManagement> list = lambdaQuery()
                .select(TgFlowProcessManagement::getId, TgFlowProcessManagement::getName,
                        TgFlowProcessManagement::getCreateCategory, TgFlowProcessManagement::getCreator)
                .in(TgFlowProcessManagement::getId, ids)
                .list();
        return Lambda.buildMap(list, TgFlowProcessManagement::getId);
    }

    @Override
    public List<FlowProcessSplitByTemplateVO> listByTemplateIds(List<Long> templateIds) {
        return this.baseMapper.listByTemplateIds(templateIds);
    }
}
