package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.sinohealth.system.dao.TgDeliverCustomerRecordDAO;
import com.sinohealth.system.domain.TgDeliverCustomerRecord;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventRequest;
import com.sinohealth.system.mapper.TgDeliverCustomerRecordMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 19:34
 */
@Repository
public class TgDeliverCustomerRecordDAOImpl extends ServiceImpl<TgDeliverCustomerRecordMapper, TgDeliverCustomerRecord> implements TgDeliverCustomerRecordDAO {

    /**
     * 组装authId
     * @param request
     * @return
     */
    @Override
    public Page<TgDeliverCustomerRecordDTO> queryParentList(DataDeliverCustomerEventRequest request) {
        return this.baseMapper.queryParentList(request);
    }

    @Override
    public List<TgDeliverCustomerRecordDTO> listChilds(List<Integer> parentIds) {
        if (CollectionUtils.isEmpty(parentIds)) {
            return Collections.EMPTY_LIST;
        }
        return this.baseMapper.listChildren(parentIds);
    }

}
