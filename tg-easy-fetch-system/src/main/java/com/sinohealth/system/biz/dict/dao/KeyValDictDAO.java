package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.KeyValDict;

import java.util.List;
import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-08-13 15:59
 */
public interface KeyValDictDAO extends IService<KeyValDict> {

    Optional<String> queryValue(String key);

    boolean updateValue(String key, String val);

    List<KeyValDict> listFlowTable();

}
