package com.sinohealth.system.service.impl;

import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DbConnectType;
import com.sinohealth.common.enums.DbType;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.service.IntegrateDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class IntegrateDataSourceServiceImpl implements IntegrateDataSourceService {

    @Value("${dsApi.uri}")
    private String uri;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public AjaxResult createDataSource(BaseDataSourceParamDto baseDataSourceParamDto) {
        String method;
        if(baseDataSourceParamDto.getId() == null){
            method = DsConstants.PROCESS_DATASOURCE_ADD;
        }else {
            method = DsConstants.PROCESS_DATASOURCE_UPDATE;
        }
        StringBuffer url = new StringBuffer(uri).append(method);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BaseDataSourceParamDto> request = new HttpEntity<>(baseDataSourceParamDto, headers);
        AjaxResult r = restTemplate.postForObject(url.toString(),request , AjaxResult.class);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult queryDataSource(int id) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_DETAIL);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
        postParameters.add("id",id);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        AjaxResult r = restTemplate.postForObject(url.toString(),request , AjaxResult.class);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult queryDataSourceListPaging(String searchVal, Integer pageNo, Integer pageSize) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_PAGE);
        url.append("?pageNo={pageNo}&pageSize={pageSize}&searchVal={searchVal}");
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("pageSize",pageSize);
        postParameters.put("pageNo",pageNo);
        postParameters.put("searchVal",searchVal);
        AjaxResult r = restTemplate.getForObject(url.toString(), AjaxResult.class,postParameters);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }


    @Override
    public AjaxResult verifyDataSourceName(String name) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_VERIFY);
        url.append("?name={name}");
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("name",name);
        AjaxResult r = restTemplate.getForObject(url.toString(), AjaxResult.class,postParameters);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult checkConnection(BaseDataSourceParamDto baseDataSourceParamDto) {
        String method = DsConstants.PROCESS_DATASOURCE_CONNECT;;
        StringBuffer url = new StringBuffer(uri).append(method);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BaseDataSourceParamDto> request = new HttpEntity<>(baseDataSourceParamDto, headers);
        AjaxResult r = restTemplate.postForObject(url.toString(),request , AjaxResult.class);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult connectionTest(int id) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_CONNECT_TEST);
        url.append("?id={id}");
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("id",id);
        AjaxResult r = restTemplate.getForObject(url.toString(), AjaxResult.class,postParameters);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult delete(int id) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_DELETE);
        url.append("?id={id}");
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("id",id);
        AjaxResult r = restTemplate.getForObject(url.toString(), AjaxResult.class,postParameters);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

    @Override
    public AjaxResult queryDataSourceList(DbType type) {
        StringBuffer url = new StringBuffer(uri).append(DsConstants.PROCESS_DATASOURCE_LIST);
        url.append("?type={type}");
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("type",type);
        AjaxResult r = restTemplate.getForObject(url.toString(), AjaxResult.class,postParameters);
        if(r.getCode() != 0){
            r  = AjaxResult.error(r.getMsg());
        }
        return r;
    }

}
