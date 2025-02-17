package com.sinohealth.system.biz.dict.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dict.constant.KeyDictType;
import com.sinohealth.system.biz.dict.dao.KeyValDictDAO;
import com.sinohealth.system.biz.dict.domain.KeyValDict;
import com.sinohealth.system.biz.dict.mapper.KeyValDictMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-08-13 15:59
 * @see KeyDictType
 */
@Repository
public class KeyValDictDAOImpl
        extends ServiceImpl<KeyValDictMapper, KeyValDict>
        implements KeyValDictDAO {

    @Override
    public Optional<String> queryValue(String key) {
        return lambdaQuery()
                .eq(KeyValDict::getName, key)
                .oneOpt().map(KeyValDict::getVal);
    }

    @Override
    public boolean updateValue(String key, String val) {
        return lambdaUpdate().set(KeyValDict::getVal, val).eq(KeyValDict::getName, key).update();
    }

    public List<KeyValDict> listFlowTable() {
        return lambdaQuery()
                .likeRight(KeyValDict::getName, KeyDictType.syncTableKeyPrefix + "%")
                .list();
    }
}
