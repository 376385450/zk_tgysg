package com.sinohealth.system.biz.scheduler.service.impl;

import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.mapper.TgCogradientInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.impl.AbstractIntegrateProcessDefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-05 18:10
 */

@Slf4j
@Service
public class IntegrateSyncProcessDefServiceImpl
        extends AbstractIntegrateProcessDefService
        implements IntegrateSyncProcessDefService {

    @Value("${dsApi.uri}")
    private String uri;
    @Value("${dsApi.syncProjectName}")
    private String projectName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private TgCogradientInfoMapper tgCogradientInfoMapper;
    @Resource
    private ITableInfoService tableInfoService;
    @Resource
    private ISysUserService sysUserService;


    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    @Override
    public TgCogradientInfoMapper getTgCogradientInfoMapper() {
        return this.tgCogradientInfoMapper;
    }

    @Override
    public ITableInfoService getTableInfoService() {
        return this.tableInfoService;
    }

    @Override
    public ISysUserService getSysUserService() {
        return this.sysUserService;
    }

}
