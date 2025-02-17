package com.sinohealth;

import com.sinohealth.arkbi.api.ExtAnalysisApi;
import com.sinohealth.arkbi.api.ExtAnalysisUserApi;
import com.sinohealth.arkbi.param.DownloadFileType;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.framework.resttempplate.model.BaseResponse;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.IMyDataDirService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.vo.ArkBIEditVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.util.List;

/**
 * 测试任务
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataPlatFormApplication.class)
@ActiveProfiles("local")
public class ApplicationTests {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationTests.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IMyDataDirService myDataDirService;

    @Test
    public void testRestTemplate() {
        String loginUrl = String.format("http://192.168.16.151:12345/dolphinscheduler/projects/list-paging?pageSize=10&pageNo=1");
        BaseResponse r = restTemplate.getForObject(loginUrl, BaseResponse.class);
        System.out.println(r);
    }

    @Autowired
    ExtAnalysisApi extAnalysisApi;


    @Autowired
    ExtAnalysisUserApi extAnalysisUserApi;

    @Test
    public void testApi() throws Exception {
        ArkBIEditVo vo = myDataDirService.createBIChart(1294L, 0, 1L);
        System.out.println("vo = " + vo);
    }

    @Autowired
    ISysUserService userService;

    @Test
    public void testGetApp() throws Exception {
        SysUser sysUser = userService.selectUserById(157L);
        ThreadContextHolder.setSysUser(sysUser);
        List applicationList = myDataDirService.getApplicationList();
        System.out.println("vo = " + applicationList);
    }

    @Test
    public void testCopyBi() throws Exception {
        SysUser sysUser = userService.selectUserById(157L);
        ThreadContextHolder.setSysUser(sysUser);
        ArkBIEditVo copy = myDataDirService.crateBICopy("f18ab5042eff42c398e382b6d6cce29d");
        System.out.println("vo = " + copy);
    }

    @Test
    public void testCopyBiToCustomer() throws Exception {
        SysUser sysUser = userService.selectUserById(157L);
        ThreadContextHolder.setSysUser(sysUser);
        ArkbiAnalysis copy = myDataDirService.createBICopyForCustomer(
                "f18ab5042eff42c398e382b6d6cce29d",
                175L
        );
        System.out.println("vo = " + copy);
    }

    @Test
    public void testDownload() throws Exception {
        SysUser sysUser = userService.selectUserById(157L);
        ThreadContextHolder.setSysUser(sysUser);
        FileOutputStream output = new FileOutputStream("test.xls");
        FileOutputStream output2 = new FileOutputStream("test2.png");
        //图表
        myDataDirService.downloadBIFile("8ee180e01a4042faad2fc258adf65bff", DownloadFileType.EXCEL, output);
        //仪表板
        myDataDirService.downloadBIFile("77f3bf21a077458b8f2f7efbf842da37", DownloadFileType.IMAGE, output2);
    }

}
