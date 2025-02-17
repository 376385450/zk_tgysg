package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.CustomFieldInfoDAO;
import com.sinohealth.system.domain.CustomFieldInfo;
import com.sinohealth.system.mapper.CustomFieldInfoMapper;
import org.springframework.stereotype.Repository;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 10:35
 */
@Repository
public class CustomFieldInfoDAOImpl extends ServiceImpl<CustomFieldInfoMapper, CustomFieldInfo> implements CustomFieldInfoDAO {
}
