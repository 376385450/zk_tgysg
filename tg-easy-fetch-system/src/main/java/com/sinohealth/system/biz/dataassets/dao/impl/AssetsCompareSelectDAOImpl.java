package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareSelectDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareSelect;
import com.sinohealth.system.biz.dataassets.mapper.AssetsCompareSelectMapper;
import org.springframework.stereotype.Repository;

/**
 * @author Kuangcp
 * 2024-07-17 21:39
 */
@Repository
public class AssetsCompareSelectDAOImpl
        extends ServiceImpl<AssetsCompareSelectMapper, AssetsCompareSelect>
        implements AssetsCompareSelectDAO {
}
