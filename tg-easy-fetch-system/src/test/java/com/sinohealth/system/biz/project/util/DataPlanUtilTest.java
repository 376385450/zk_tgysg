package com.sinohealth.system.biz.project.util;

import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.biz.project.domain.DataPlan;
import com.sinohealth.system.biz.project.dto.request.BizTypePlanVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-12-13 16:15
 */
@Slf4j
public class DataPlanUtilTest {

    @Test
    public void testCtx() throws Exception {
        DataPlanCtx ctx = new DataPlanCtx(20, 28, 36, 1);
        log.info(": ctx={}", ctx);
    }

    @Test
    public void testBuildPlans() throws Exception {
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(21);
        cmh.setSop(39);
        cmh.setDeliver(42);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 1, Collections.singletonList(cmh));
        for (DataPlan plan : result) {
//            log.info("plan={}", plan);
        }
    }

    @Test
    public void testBuildPlans2() throws Exception {
        List<BizTypePlanVo> plans = new ArrayList<>();
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(1);
        cmh.setSop(14);
        cmh.setDeliver(37);


        BizTypePlanVo o2o = new BizTypePlanVo();
        o2o.setBizType(BizTypeEnum.O2O.name());
        o2o.setQc(1);
        o2o.setSop(14);
        o2o.setDeliver(37);
        plans.add(cmh);
        plans.add(o2o);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 2, plans);
        for (DataPlan plan : result) {
//            log.info("plan={}", plan);
        }
    }

    @Test
    public void testBuildPlans3() throws Exception {
        List<BizTypePlanVo> plans = new ArrayList<>();
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(0);
        cmh.setSop(14);
        cmh.setDeliver(37);


        BizTypePlanVo o2o = new BizTypePlanVo();
        o2o.setBizType(BizTypeEnum.O2O.name());
        o2o.setQc(1);
        o2o.setSop(14);
        o2o.setDeliver(37);
        plans.add(cmh);
        plans.add(o2o);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 2, plans);
        for (DataPlan plan : result) {
//            log.info("plan={}", plan);
        }
    }

    @Test
    public void testBuildPlans4() throws Exception {
        List<BizTypePlanVo> plans = new ArrayList<>();
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(0);
        cmh.setSop(0);
        cmh.setDeliver(37);


        BizTypePlanVo o2o = new BizTypePlanVo();
        o2o.setBizType(BizTypeEnum.O2O.name());
        o2o.setQc(1);
        o2o.setSop(14);
        o2o.setDeliver(37);
        plans.add(cmh);
        plans.add(o2o);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 2, plans);
        for (DataPlan plan : result) {
//            log.info("plan={}", plan);
        }
    }

    @Test
    public void testPlansOver() throws Exception {
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(20);
        cmh.setSop(35);
        cmh.setDeliver(37);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 2, Collections.singletonList(cmh), true);
        for (DataPlan plan : result) {
//            log.info("plan={}", plan);
        }
    }

    @Test
    public void testOnlyDelivery() throws Exception {
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(0);
        cmh.setSop(0);
        cmh.setDeliver(15);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 1, Collections.singletonList(cmh));
    }

    @Test
    public void testOnlyDeliveryOverMonth() throws Exception {
        BizTypePlanVo cmh = new BizTypePlanVo();
        cmh.setBizType(BizTypeEnum.cmh.name());
        cmh.setQc(0);
        cmh.setSop(0);
        cmh.setDeliver(65);
        List<DataPlan> result = DataPlanUtil.buildPlans(12L, 2, Collections.singletonList(cmh));
    }
}
