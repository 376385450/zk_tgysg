package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareFileDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareFile;
import com.sinohealth.system.biz.dataassets.mapper.AssetsCompareFileMapper;
import org.springframework.stereotype.Repository;

/**
 * @author Kuangcp
 * 2024-07-17 21:41
 */
@Repository
public class AssetsCompareFileDAOImpl
        extends ServiceImpl<AssetsCompareFileMapper, AssetsCompareFile>
        implements AssetsCompareFileDAO {
}
