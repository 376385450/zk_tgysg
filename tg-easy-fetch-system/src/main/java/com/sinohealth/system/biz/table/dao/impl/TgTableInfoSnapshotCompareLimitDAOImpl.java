package com.sinohealth.system.biz.table.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareLimitDAO;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareLimit;
import com.sinohealth.system.biz.table.mapper.TgTableInfoSnapshotCompareLimitMapper;
import org.springframework.stereotype.Repository;

/**
 * @author monster
 * @Date 2024-07-11 10:56
 */
@Repository
public class TgTableInfoSnapshotCompareLimitDAOImpl
        extends ServiceImpl<TgTableInfoSnapshotCompareLimitMapper, TgTableInfoSnapshotCompareLimit>
        implements TgTableInfoSnapshotCompareLimitDAO {
}
