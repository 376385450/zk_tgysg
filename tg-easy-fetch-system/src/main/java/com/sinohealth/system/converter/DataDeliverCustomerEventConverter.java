package com.sinohealth.system.converter;

import com.google.common.collect.Lists;
import com.sinohealth.system.domain.TgDeliverCustomerRecord;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventVO;

import java.util.Objects;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 20:34
 */
public class DataDeliverCustomerEventConverter {

    public static DataDeliverCustomerEventVO convert(TgDeliverCustomerRecord record) {
        DataDeliverCustomerEventVO eventDTO = new DataDeliverCustomerEventVO();
        eventDTO.setId(record.getId());
        eventDTO.setAllocateType(Objects.equals(record.getAllocateType(), 1) ? "打包" : "单份");
        eventDTO.setTableName(record.getTableName());
        eventDTO.setTableId(record.getTableId());
        eventDTO.setProjectName(record.getProjectName());
        eventDTO.setAuthUserName(record.getAllocateUserName());
        eventDTO.setAuthType(record.getAuthType());
        eventDTO.setIcon(record.getIcon());
        eventDTO.setNodeId(record.getNodeId());
        eventDTO.setItemList(Lists.newArrayList());
        return eventDTO;
    }

    public static DataDeliverCustomerEventVO convert(TgDeliverCustomerRecordDTO record) {
        DataDeliverCustomerEventVO eventDTO = new DataDeliverCustomerEventVO();
        eventDTO.setId(record.getId());
        eventDTO.setAllocateType(Objects.equals(record.getAllocateType(), 1) ? "打包" : "单份");
        eventDTO.setTableName(record.getTableName());
        eventDTO.setTableId(record.getTableId());
        eventDTO.setProjectName(record.getProjectName());
        eventDTO.setAuthUserName(record.getAllocateUserName());
        eventDTO.setAuthType(record.getAuthType());
        eventDTO.setIcon(record.getIcon());
        eventDTO.setNodeId(record.getNodeId());
        eventDTO.setAuthId(record.getAuthId());
        eventDTO.setAuthStatus(record.getAuthStatus());
        eventDTO.setItemList(Lists.newArrayList());
        return eventDTO;
    }
}
