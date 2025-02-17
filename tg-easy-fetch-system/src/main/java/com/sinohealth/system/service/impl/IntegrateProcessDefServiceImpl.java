package com.sinohealth.system.service.impl;

import com.sinohealth.system.mapper.TgCogradientInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
/**
 * @author zhangyanping
 * @date 2023/7/6 16:24
 */

@Log4j2
@Service
public class IntegrateProcessDefServiceImpl extends AbstractIntegrateProcessDefService implements IntergrateProcessDefService {

    @Value("${dsApi.uri}")
    private String uri;
    @Value("${dsApi.projectName}")
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
