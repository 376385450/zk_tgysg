package com.sinohealth.system.biz.application.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigSnapshotDAO;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfigSnapshot;
import com.sinohealth.system.biz.application.mapper.ApplicationTaskConfigSnapshotMapper;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-05 15:39
 */
@Repository
public class ApplicationTaskConfigSnapshotDAOImpl
        extends ServiceImpl<ApplicationTaskConfigSnapshotMapper, ApplicationTaskConfigSnapshot>
        implements ApplicationTaskConfigSnapshotDAO {
}
