package com.sinohealth.system.biz.table.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotComparePlanDAO;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotComparePlan;
import com.sinohealth.system.biz.table.mapper.TgTableInfoSnapshotComparePlanMapper;
import org.springframework.stereotype.Repository;

/**
 * @author monster
 * @Date 2024-07-18 10:56
 */
@Repository
public class TgTableInfoSnapshotComparePlanDAOImpl
        extends ServiceImpl<TgTableInfoSnapshotComparePlanMapper, TgTableInfoSnapshotComparePlan>
        implements TgTableInfoSnapshotComparePlanDAO {
}
