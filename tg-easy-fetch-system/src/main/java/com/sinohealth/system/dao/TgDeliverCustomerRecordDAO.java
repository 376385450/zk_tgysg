package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.Page;
import com.sinohealth.system.domain.TgDeliverCustomerRecord;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventRequest;

import java.util.List;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 19:33
 */
public interface TgDeliverCustomerRecordDAO extends IService<TgDeliverCustomerRecord> {

    Page<TgDeliverCustomerRecordDTO> queryParentList(DataDeliverCustomerEventRequest request);

    List<TgDeliverCustomerRecordDTO> listChilds(List<Integer> parentIds);
}
