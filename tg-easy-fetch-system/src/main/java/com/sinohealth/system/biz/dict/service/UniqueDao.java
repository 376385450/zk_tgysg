package com.sinohealth.system.biz.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-04 09:41
 */
public interface UniqueDao<T> {

    List<T> queryAllForUnique(LambdaQueryWrapper<T> query);
}
