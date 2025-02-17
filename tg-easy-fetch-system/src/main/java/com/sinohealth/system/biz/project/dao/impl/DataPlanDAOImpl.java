package com.sinohealth.system.biz.project.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.project.dao.DataPlanDAO;
import com.sinohealth.system.biz.project.domain.DataPlan;
import com.sinohealth.system.biz.project.mapper.DataPlanMapper;
import org.springframework.stereotype.Repository;

/**
 * @author Kuangcp
 * 2024-12-13 14:25
 */
@Repository
public class DataPlanDAOImpl
        extends ServiceImpl<DataPlanMapper, DataPlan>
        implements DataPlanDAO {


}
