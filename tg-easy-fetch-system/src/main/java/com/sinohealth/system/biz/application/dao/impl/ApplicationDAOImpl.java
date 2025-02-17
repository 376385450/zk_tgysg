package com.sinohealth.system.biz.application.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 14:23
 */
@Repository
public class ApplicationDAOImpl
        extends ServiceImpl<TgApplicationInfoMapper, TgApplicationInfo>
        implements ApplicationDAO {
}
