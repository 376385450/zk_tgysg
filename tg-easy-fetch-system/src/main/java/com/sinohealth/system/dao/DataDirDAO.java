package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.entity.DataDir;

import java.util.Collection;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-11 10:55
 */
public interface DataDirDAO extends IService<DataDir> {

    Map<Long, String> queryParentMap(Collection<Long> ids);
}
