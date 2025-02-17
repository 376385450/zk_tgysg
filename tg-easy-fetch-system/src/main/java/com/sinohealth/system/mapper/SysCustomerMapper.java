package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysCustomerMapper extends BaseMapper<SysCustomer> {

    void updateByUserId(SysCustomer sysCustomer);

    SysCustomer getByUserId(Long userId);

    List<SysUser> queryList(@Param("manageUserId") Integer manageUserId, @Param("status") Integer status, @Param("searchVal") String searchVal);

    List<SysUser>  selectSubList( @Param("status") Integer status, @Param("searchVal") String searchVal,@Param("parentAccountId")Integer  parentAccountId );

    void updateByBatchIds(@Param("ids") List<Long> ids,@Param("manageId")Integer manageId);

    List<SysUser> selectManageUser();

    Map<String,Object> getUserCount();

    List<String> selectNames(@Param("ids") String ids);

    List<SysCustomer> listAllCustomers();
}
