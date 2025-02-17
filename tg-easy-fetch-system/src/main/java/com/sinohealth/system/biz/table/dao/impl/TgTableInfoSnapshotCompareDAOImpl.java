package com.sinohealth.system.biz.table.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDAO;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompare;
import com.sinohealth.system.biz.table.mapper.TgTableInfoSnapshotCompareMapper;
import org.springframework.stereotype.Repository;

/**
 * @author monster
 * @Date 2024-07-11 10:56
 */
@Repository
public class TgTableInfoSnapshotCompareDAOImpl
        extends ServiceImpl<TgTableInfoSnapshotCompareMapper, TgTableInfoSnapshotCompare>
        implements TgTableInfoSnapshotCompareDAO {
}
