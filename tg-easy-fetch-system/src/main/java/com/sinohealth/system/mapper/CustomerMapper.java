package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.customer.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
