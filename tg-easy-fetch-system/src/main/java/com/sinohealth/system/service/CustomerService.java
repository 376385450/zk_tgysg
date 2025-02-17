package com.sinohealth.system.service;

import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.system.dto.customer.PageQueryCustomer;
import com.sinohealth.system.dto.customer.SaveCustomerReq;
import com.sinohealth.system.vo.CustomerVO;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
public interface CustomerService {

    PageInfo<CustomerVO> page(PageQueryCustomer page);

    void save(SaveCustomerReq req);

}
