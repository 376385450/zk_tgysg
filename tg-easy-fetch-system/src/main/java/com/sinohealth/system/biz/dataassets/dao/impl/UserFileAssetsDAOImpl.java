package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dataassets.dao.UserFileAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.biz.dataassets.mapper.UserFileAssetsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 15:13
 */
@Slf4j
@Repository
public class UserFileAssetsDAOImpl
        extends ServiceImpl<UserFileAssetsMapper, UserFileAssets>
        implements UserFileAssetsDAO {

}
