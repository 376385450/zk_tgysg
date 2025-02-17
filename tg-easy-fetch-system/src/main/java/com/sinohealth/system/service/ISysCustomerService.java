package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Map;


public interface ISysCustomerService extends IService<SysCustomer> {

     int insert(SysCustomer sysCustomer);

     void update(SysCustomer sysCustomer);

    SysCustomer getByUserId(Long userId);

    List<SysUser> selectList(Integer manageUserId, Integer status, String searchVal);

    List<SysUser> selectSubList( Integer status, String searchVal,Integer  parentAccountId );

    void updateByBatchIds(List<Long> ids,Integer manageId);

    List<SysUser> selectManageUser();

    Map<String,Object> getUserCount();

    String getClientNames(String ids);

    List<SysCustomer> listAllCustomers();
}
