package com.sinohealth.common.utils;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.ApiClient;
import com.sinohealth.ipaas.SignInterceptor;
import com.sinohealth.ipaas.model.*;
import com.sinohealth.ipaas.openapi.MaindataApiApi;
import com.sinohealth.ipaas.openapi.MaindataDeptApi;
import feign.RequestInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class SinoipaasUtils {

    private static String appId;

    private static String appsecret;

    private static String basepath;

    private static MaindataApiApi api;

    private static MaindataDeptApi deptApi;

    @Value("${sinohealth.maindata.gateway.appId}")
    private String Y_T_APPID;

    @Value("${sinohealth.maindata.gateway.appsecret}")
    private String token;

    @Value("${sinohealth.maindata.gateway.url}")
    private String url;

    public static Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(2))
            .build();

    @PostConstruct
    public void init() {
        appId = Y_T_APPID;
        appsecret = token;
        basepath = url;
        RequestInterceptor ri = new SignInterceptor(appsecret);
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basepath);
        apiClient.addAuthorization(ri.getClass().getName(), ri);
        api = apiClient.buildClient(MaindataApiApi.class);
        deptApi = apiClient.buildClient(MaindataDeptApi.class);
    }

    public static SinoPassUserDTO mainEmployeeSelectbyid(String employeeId) {
        String key = "sino_pass:employee:" + employeeId;
        return (SinoPassUserDTO) cache.get(key, s -> queryUserByApi(employeeId));
    }

    // TODO 批量接口
    public static List<SinoPassUserDTO> mainEmployeeByIds(Set<String> ids) {
        ResMaindatamainEmployeeselectbyidsItem rsp = api.employeeSelectbyidsPOST(appId, null, null,
                null, new ArrayList<>(ids), null);
        List<ResMaindatamainEmployeeselectbyidsItemDataItem> items = rsp.getData();
        return items.stream().map(SinoPassUserDTO::new).collect(Collectors.toList());
    }

    private static SinoPassUserDTO queryUserByApi(String employeeId) {
        ReqMaindataemployeepageListitem req = new ReqMaindataemployeepageListitem();
        req.setLimit(new BigDecimal("10"));
        req.setOffset(new BigDecimal("0"));
        req.setId(employeeId);
        ResMaindataemployeepageListItem rsp = api.employeePagelistPOST(appId, null, null, null,
                req, null);

        // 员工信息
        List<ResMaindataemployeepageListItemDataItem> data = rsp.getData();
        if (data != null && !data.isEmpty()) {
            return new SinoPassUserDTO(data.get(0));
        } else {
            log.info("MDM查询用户信息不存在：{}", employeeId);
            return null;
        }
    }

    public static List<SinoPassUserDTO> employeeSelectbypage(String userName, Integer start, Integer limit) {
        // 分页查询
        ReqMaindataemployeepageListitem reqMaindataemployeepageListitem = new ReqMaindataemployeepageListitem();
        reqMaindataemployeepageListitem.setLimit(new BigDecimal(limit));
        reqMaindataemployeepageListitem.setOffset(new BigDecimal(start));
        reqMaindataemployeepageListitem.setUserName(userName);
        ResMaindataemployeepageListItem resMaindataemployeepageListItem = api.employeePagelistPOST(appId, null, null, null, reqMaindataemployeepageListitem, "");
        List<ResMaindataemployeepageListItemDataItem> data = resMaindataemployeepageListItem.getData();
        if (data != null && data.size() > 0) {
            List<SinoPassUserDTO> list = new ArrayList<>();
            for (ResMaindataemployeepageListItemDataItem resMaindataemployeepageListItemDataItem : data) {
                SinoPassUserDTO sinoPassUserDTO = new SinoPassUserDTO(resMaindataemployeepageListItemDataItem);
                list.add(sinoPassUserDTO);
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<ResMaindataMainDepartmentSelectAllItemDataItem> mainDepartmentAll() {
        final ResMaindataMainDepartmentSelectAllItem resMaindataMainDepartmentSelectAllItem = deptApi.maindataMainDepartmentSelectAllGET(appId, null, null, null, null);
        final List<ResMaindataMainDepartmentSelectAllItemDataItem> data = resMaindataMainDepartmentSelectAllItem.getData();
        if (data != null && data.size() > 0) {
            return data;
        } else {
            return null;
        }
    }

    public static List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> employeeWithDept(List<String> mainOrgUserIds) {
        final ReqMaindataMainDepartmentSelectUserWithDeptitem req = new ReqMaindataMainDepartmentSelectUserWithDeptitem();
        req.setIds(mainOrgUserIds);
        final ResMaindataMainDepartmentSelectUserWithDeptItem rsp = deptApi.maindataMainDepartmentSelectUserWithDeptPOST(appId,
                null, null, null, req, null);
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> data = rsp.getData();
        if (data != null && !data.isEmpty()) {
            return data;
        } else {
            return null;
        }
    }


    public static ResMaindatamainDepartmentselectbyidsItemDataItem mainDepartmentSelectbyids(String... depts) {
        // 部门
        ResMaindatamainDepartmentselectbyidsItem resMaindatamainDepartmentselectbyidsItem = api.departmentSelectbyidsPOST(appId, null, null, null, Arrays.asList(depts), null);
        List<ResMaindatamainDepartmentselectbyidsItemDataItem> data = resMaindatamainDepartmentselectbyidsItem.getData();
        if (data != null && data.size() > 0) {
            return data.get(0);
        } else {
            return null;
        }
    }

    public static List<ResMaindatamainDepartmentselectbyidsItemDataItem> mainDepartmentSelectbyids(List<String> depts) {
        if (CollectionUtils.isEmpty(depts)) {
            return Collections.emptyList();
        }
        depts = new ArrayList<>(new HashSet<>(depts));

        List<ResMaindatamainDepartmentselectbyidsItemDataItem> cacheDept = new ArrayList<>();
        List<String> queryDept = new ArrayList<>();
        for (String dept : depts) {
            final Object ifPresent = cache.getIfPresent(dept);
            if (Objects.nonNull(ifPresent)) {
                cacheDept.add((ResMaindatamainDepartmentselectbyidsItemDataItem) ifPresent);
            } else {
                queryDept.add(dept);
            }
        }

        // 部门
        if (CollUtil.isNotEmpty(queryDept)) {
            ResMaindatamainDepartmentselectbyidsItem resMaindatamainDepartmentselectbyidsItem = api.departmentSelectbyidsPOST(appId, null, null, null, queryDept, null);
            List<ResMaindatamainDepartmentselectbyidsItemDataItem> data = resMaindatamainDepartmentselectbyidsItem.getData();
            if (data != null && !data.isEmpty()) {
                cacheDept.addAll(data);
                for (ResMaindatamainDepartmentselectbyidsItemDataItem item : data) {
                    cache.put(item.getId(), item);
                }
            }
        }

        return cacheDept;

    }

    public static List<ResMaindatamasterCompanyselectbypageItemDataItem> masterCompanySelectbypage(String companyName, Integer start, Integer limit) {
        // 企业
        ReqMaindatamasterCompanyselectbypageitem reqMasterCompitem = new ReqMaindatamasterCompanyselectbypageitem();
        reqMasterCompitem.setCompanyName(companyName);
        reqMasterCompitem.setOffset(new BigDecimal(start));
        reqMasterCompitem.setLimit(new BigDecimal(limit));
        ResMaindatamasterCompanyselectbypageItem resMasterComp4p = api.companySelectbypagePOST(appId, null, null, null, reqMasterCompitem, null);
        List<ResMaindatamasterCompanyselectbypageItemDataItem> data = resMasterComp4p.getData();
        return data;
    }

}
