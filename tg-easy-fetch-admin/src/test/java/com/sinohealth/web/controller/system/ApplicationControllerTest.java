package com.sinohealth.web.controller.system;

import com.sinohealth.DataPlatFormApplication;
import com.sinohealth.system.dto.application.ApplicationConfigRequest;
import com.sinohealth.system.service.IApplicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author zhangyanping
 * @date 2023/6/25 16:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataPlatFormApplication.class)
@ActiveProfiles("test")
public class ApplicationControllerTest {

    @Resource
    private IApplicationService applicationService;
    @Resource
    private ApplicationApiController controller;

    @Test
    public void executeWorkFlow() {

        ApplicationConfigRequest request = new ApplicationConfigRequest();
        request.setId(129L);
        request.setType(1);
        request.setWorkflowId(1576);
        request.setSql("select * from edw.dim_project_auto");
        controller.config(request);
        System.out.println("保存工作流");
    }


    @Test
    public void executeWorkFlow2() {
        controller.executeWorkFlow(79L);
        System.out.println("保存工作流");
    }
}
