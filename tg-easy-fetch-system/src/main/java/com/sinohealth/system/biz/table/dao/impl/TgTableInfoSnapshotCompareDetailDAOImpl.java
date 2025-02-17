package com.sinohealth.system.biz.table.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDetailDAO;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareDetail;
import com.sinohealth.system.biz.table.mapper.TgTableInfoSnapshotCompareDetailMapper;
import org.springframework.stereotype.Repository;

/**
 * @author monster
 * @Date 2024-07-11 10:56
 */
@Repository
public class TgTableInfoSnapshotCompareDetailDAOImpl
        extends ServiceImpl<TgTableInfoSnapshotCompareDetailMapper, TgTableInfoSnapshotCompareDetail>
        implements TgTableInfoSnapshotCompareDetailDAO {
}
