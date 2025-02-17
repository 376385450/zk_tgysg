package com.sinohealth.system.biz.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-04 09:41
 */
public interface UniqueDomain<T> {

    Long getId();

    String getBizType();

    String getBizName();

    void appendQuery(LambdaQueryWrapper<T> wrapper);

}
