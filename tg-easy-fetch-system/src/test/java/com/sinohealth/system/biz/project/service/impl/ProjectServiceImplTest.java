package com.sinohealth.system.biz.project.service.impl;

import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.domain.ProjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-11 16:05
 */
@Slf4j
public class ProjectServiceImplTest {


    @Test
    public void testCalcRelation() throws Exception {
        List<ProjectHelper> exists = new ArrayList<>();

        List<ProjectHelper> except = new ArrayList<>();

        IntStream.rangeClosed(1, 9).mapToObj(v -> {
            ProjectHelper h = new ProjectHelper();
            h.setProjectId((long) v);
            h.setUserId(1L);
            return h;
        }).forEach(exists::add);

        IntStream.rangeClosed(3, 9).mapToObj(v -> {
            ProjectHelper h = new ProjectHelper();
            h.setProjectId((long) v);
            h.setUserId((long) (v % 2));
            return h;
        }).forEach(except::add);


        Map<Long, Set<Long>> exceptMap = Lambda.buildGroupMapSet(except, ProjectHelper::getProjectId, ProjectHelper::getUserId);
        log.info("except={}", exceptMap);

        List<ProjectHelper> result = ProjectServiceImpl.calcNeedSaveRelation(exceptMap, exists);
        log.info("result={}", result);

        Map<Long, Set<Long>> rMap = Lambda.buildGroupMapSet(result, ProjectHelper::getProjectId, ProjectHelper::getUserId);
        log.info("result={}", rMap);

        Map<Long, String> userIdsMap = Lambda.buildGroupMapString(except, ProjectHelper::getUserId, v -> v.getProjectId() + "", ",");
        log.info("userIdsMap={}", userIdsMap);


        // 4,0 6,0 8,0
        assert rMap.size() == 3;

    }
}
