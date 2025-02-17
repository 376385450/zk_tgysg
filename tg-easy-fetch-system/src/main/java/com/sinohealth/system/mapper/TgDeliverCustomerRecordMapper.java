package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sinohealth.system.domain.TgDeliverCustomerRecord;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 19:33
 */
@Mapper
public interface TgDeliverCustomerRecordMapper extends BaseMapper<TgDeliverCustomerRecord> {

    Page<TgDeliverCustomerRecordDTO> queryParentList(DataDeliverCustomerEventRequest request);

    List<TgDeliverCustomerRecordDTO> listChildren(List<Integer> parentIds);
}
