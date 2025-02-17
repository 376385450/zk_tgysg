package com.sinohealth.system.biz.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.process.dao.TgFlowProcessCheckDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessCheck;
import com.sinohealth.system.biz.process.dto.CreateAutoProcessRequest;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.biz.process.vo.DqcQcLogVO;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.dto.common.PageRequest;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-08-08 19:20
 */
@Slf4j
@Service
@AllArgsConstructor
public class TgFlowProcessCheckServiceImpl implements TgFlowProcessCheckService {

    private final TgCkProviderMapper ckProviderMapper;
    private final TgFlowProcessCheckDAO flowProcessCheckDAO;
    private final TgTemplateInfoMapper templateInfoMapper;

    private final ISysUserService sysUserService;
    private final DataPlanService dataPlanService;

    private static final String mainSQL = "select period,prodcode,remarks,out_prodcode,sop_time,update_date " +
            "from cmh_qc_sop_mirror " +
            "where upper (type) = 'CMH' and qc_leader != '暂无' and upper(prodcode) like 'P%' ";

    private static final String pageCountSQL = "select count(*) from cmh_qc_sop_mirror " +
            "where upper(type) = 'CMH' and qc_leader != '暂无' and upper(prodcode)  like 'P%'";

    @Override
    public AjaxResult<IPage<DqcQcLogVO>> pageQuery(PageRequest pageRequest) {
        Long total = ckProviderMapper.countAllDataFromCk(pageCountSQL);
        IPage<DqcQcLogVO> pageResult = new Page<>();
        pageResult.setTotal(total);

        List<LinkedHashMap<String, Object>> data = ckProviderMapper.selectAllDataFromCk(
                mainSQL + " order by update_date desc limit " + pageRequest.buildOffset() + "," + pageRequest.getSize()
        );
        List<DqcQcLogVO> list = this.dataToVOList(data);
        pageResult.setRecords(list);

        pageResult.setPages(pageRequest.getPage());
        pageResult.setSize(pageRequest.getSize());
        return AjaxResult.success(pageResult);
    }

    private Pair<FlowProcessTypeEnum, List<String>> buildState(List<DqcQcLogVO> list) {
        boolean finishSOP = list.stream().allMatch(v -> Objects.nonNull(v.getSopTime()));
        if (finishSOP) {
            return Pair.of(FlowProcessTypeEnum.deliver, Collections.emptyList());
        }

        boolean finishQC = list.stream().allMatch(v -> Objects.nonNull(v.getQcTime()));
        if (finishQC) {
            return Pair.of(FlowProcessTypeEnum.sop, Collections.emptyList());
        }

        // 昨天完成QC的品类, 去重（单品类会有多子品类分开QC）
        List<String> prodList = list.stream()
                .filter(v -> Objects.nonNull(v.getQcTime()) && v.getQcTime().isEqual(LocalDate.now().plusDays(-1)))
                .map(DqcQcLogVO::getProdcode)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(prodList)) {
            return null;
        }
        return Pair.of(FlowProcessTypeEnum.qc, prodList);
    }

    private List<String> buildDiffProdList(List<DqcQcLogVO> list, FlowProcessTypeEnum nowType) {
        if (FlowProcessTypeEnum.isOnceType(nowType.name())) {
            return Collections.emptyList();
        }

        // 更新时间在昨天 QC的品类, 去重（单品类会有多子品类分开QC）
        List<String> prodList = list.stream()
                .filter(v -> Objects.nonNull(v.getQcTime()) && v.getQcTime().isEqual(LocalDate.now().plusDays(-1)))
                .map(DqcQcLogVO::getProdcode)
                .distinct()
                .collect(Collectors.toList());
        // QC 类型且无改动
        if (CollectionUtils.isEmpty(prodList)) {
            return null;
        }
        return prodList;
    }


    private List<DqcQcLogVO> dataToVOList(List<LinkedHashMap<String, Object>> data) {
        return data.stream().map(v -> {

            String qcTimeStr = string(v.get("out_prodcode"));
            LocalDate qcTime = Optional.ofNullable(qcTimeStr).map(LocalDate::parse).orElse(null);

            String sopTime = string(v.get("sop_time"));
            LocalDateTime updateDate = LocalDateTime.parse(string(v.get("update_date")));
            return DqcQcLogVO.builder()
                    .period(string(v.get("period")))
                    .prodcode(string(v.get("prodcode")))
                    .remark(string(v.get("remarks")))
                    .qcTime(qcTime)
                    .qcState(Optional.ofNullable(qcTime).map(x -> "已完成").orElse("-"))
                    .sopTime(sopTime)
                    .sopState(Optional.ofNullable(sopTime).map(x -> "已完成").orElse("-"))
//                    .updateTime(DateUtils.parseDateTime(string(v.get("update_date")).replace("T", " ")))
                    .updateTime(updateDate)
                    .build();
        }).collect(Collectors.toList());
    }

    private String string(Object val) {
        return Optional.ofNullable(val).map(Object::toString).orElse(null);
    }

    /**
     * 四种触发情况：
     * <p>
     * 新一期QC时，全量同步走QC类型全流程
     * 当期增量品类完成QC 增量同步走QC类型全流程 仅该类型可重复执行
     * 当期数据全部完成QC，全量同步走SOP类型全流程
     * 当期数据全部完成SOP，全量同步走交付类型全流程
     */
    @Override
    public Optional<CreateAutoProcessRequest> buildReqByCheck() {
        AjaxResult<CurrentDataPlanDTO> plan = dataPlanService.curPeriod(BizTypeEnum.cmh.name());
        CurrentDataPlanDTO latest = plan.getData();

        FlowProcessTypeEnum nowType = FlowProcessTypeEnum.of(latest.getFlowProcessType());
        if (Objects.isNull(nowType)) {
            log.info("QC 未开始 {}", latest);
            return Optional.empty();
        }

        Optional<TgFlowProcessCheck> lastOpt = flowProcessCheckDAO.lambdaQuery()
                .orderByDesc(TgFlowProcessCheck::getId)
                .last(" limit 1")
                .oneOpt();
        if (!lastOpt.isPresent()) {
            log.warn("全流程异常", new RuntimeException("DQC数据 未初始化"));
            return Optional.empty();
        }

        TgFlowProcessCheck lastCheck = lastOpt.get();

        // DQC 最新完成期数
//        List<LinkedHashMap<String, Object>> latestPeriod = ckProviderMapper.selectAllDataFromCk(
//                mainSQL + " AND out_prodcode is not null order by update_date desc limit 1");
//        String period = string(latestPeriod.get(0).get("period"));
//        if (!Objects.equals(period, lastCheck.getPeriod())) {
//            log.warn("DQC平台已开始新期数，但是排期未开始 {} {}",
//                    period, latest, new RuntimeException("DQC平台已开始新期数，但是全流程排期未开始"));
//            return Optional.empty();
//        }

        String period = latest.getPeriod();
        List<LinkedHashMap<String, Object>> data = ckProviderMapper.selectAllDataFromCk(
                mainSQL + " AND period ='" + period + "' order by update_date desc");
        List<DqcQcLogVO> lastList = this.dataToVOList(data);

        // 首次开始QC
        if (Objects.equals(nowType, FlowProcessTypeEnum.qc)
                && !Objects.equals(lastCheck.getProcessType(), FlowProcessTypeEnum.qc.name())) {
            String changeProdCode = lastList.stream().map(DqcQcLogVO::getProdcode)
                    .collect(Collectors.joining(","));
            TgFlowProcessCheck processCheck = new TgFlowProcessCheck()
                    .setPeriod(period)
                    .setProcessType(FlowProcessTypeEnum.qc.name())
                    .setQcProdcode(changeProdCode);
            flowProcessCheckDAO.save(processCheck);

            // 全量更新
            CreateAutoProcessRequest req = new CreateAutoProcessRequest();
            req.setPeriod(period);
            req.setType(FlowProcessTypeEnum.qc);
            return Optional.of(req);
        }

        // 限制 SOP 交付 不能重复运行
        if (FlowProcessTypeEnum.isOnceType(nowType.name()) &&
                Objects.equals(lastCheck.getProcessType(), nowType.name())) {
            log.warn("repeat run: {}", nowType);
            return Optional.empty();
        }

        List<String> diffList = this.buildDiffProdList(lastList, nowType);
        if (Objects.isNull(diffList)) {
            log.info("QC阶段 无变化");
            return Optional.empty();
        }

        TgFlowProcessCheck processCheck = new TgFlowProcessCheck()
                .setPeriod(period)
                .setProcessType(nowType.name())
                .setQcProdcode(String.join(",", diffList));
        flowProcessCheckDAO.save(processCheck);

        CreateAutoProcessRequest req = new CreateAutoProcessRequest();
        req.setPeriod(period);
        req.setType(nowType);
        req.setProdCodes(diffList);
        return Optional.of(req);
    }

    @Override
    public AjaxResult<String> queryCurPeriod(Long templateId) {
        TgTemplateInfo info = templateInfoMapper.selectOne(new QueryWrapper<TgTemplateInfo>().lambda()
                .eq(TgTemplateInfo::getId, templateId));
        if (Objects.isNull(info) || !Objects.equals(info.getBizType(), BizTypeEnum.cmh.name())) {
            return AjaxResult.success("", "");
        }

        AjaxResult<CurrentDataPlanDTO> periodRes = dataPlanService.curPeriod(info.getBizType());
        Optional<CurrentDataPlanDTO> planOpt = Optional.ofNullable(periodRes).map(AjaxResult::getData);

        String type = planOpt.map(CurrentDataPlanDTO::getFlowProcessType).orElse("");
        if (!FlowProcessTypeEnum.dev.contains(type)) {
            return AjaxResult.success("", "");
        }

        String period = planOpt.map(CurrentDataPlanDTO::getPeriod).orElse("");
        return AjaxResult.success("", period + "-" + FlowProcessTypeEnum.getDescByName(type));
    }

//    @Override
//    public AjaxResult<DqcLatestStateVO> queryCurPeriod() {
//        TgFlowProcessPlan plan = flowProcessPlanDAO.latest();
//
//        String type = keyValDictDAO.queryFlowProcessType().orElse("");
//        String period = keyValDictDAO.queryCurPeriod().orElse("");
//
//        DqcLatestStateVO vo = new DqcLatestStateVO()
//                .setPeriod(period)
//                .setFlowProcessType(type)
//                .setNextPeriod(DateUtils.formatYM(plan.getPeriodDate().plusMonths(1)))
//                .setCreate(LocalDate.now().isAfter(plan.getDeliverDate()));
//        return AjaxResult.success(vo);
//    }

    /**
     * 分页 全流程 排期
     */
//    @Override
//    public AjaxResult<IPage<TgFlowProcessPlanPageVO>> pageQueryPlan(PageRequest pageRequest) {
//        IPage<TgFlowProcessPlan> pageResult = flowProcessPlanDAO.lambdaQuery()
//                .orderByDesc(TgFlowProcessPlan::getPeriodDate)
//                .page(pageRequest.buildPage());
//        List<TgFlowProcessPlan> records = pageResult.getRecords();
//        List<Long> userIds = Lambda.buildList(records, TgFlowProcessPlan::getUpdater);
//        Map<Long, String> userNameMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserNameMapByIds);
//
//        return AjaxResult.success(PageUtil.convertMap(pageResult, v -> {
//            TgFlowProcessPlanPageVO vo = new TgFlowProcessPlanPageVO();
//            BeanUtils.copyProperties(v, vo);
//
//            if (BooleanUtils.isTrue(v.getCurrentPeriod())) {
////                vo.setEdit(LocalDate.now().isAfter(v.getDeliverDate()));
//                vo.setEdit(true);
//            }
//            vo.setUpdater(userNameMap.getOrDefault(v.getUpdater(), "系统"));
//            return vo;
//        }));
//    }

    /**
     *
     */
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public AjaxResult<Void> upsertPlan(TgFlowProcessPlanUpsertRequest request) {
//        if (!request.getQcDate().isBefore(request.getSopDate())
//                || !request.getSopDate().isBefore(request.getDeliverDate())) {
//            return AjaxResult.error("请填写准确的排期时间，必须满足QC开始时间<SOP开始时间<交付开始时间");
//        }
//
//        Long userId = SecurityUtils.getUserId();
//        LocalDate now = LocalDate.now();
//        if (Objects.nonNull(request.getId())) {
//            TgFlowProcessPlan old = flowProcessPlanDAO.getById(request.getId());
//
//            Optional<AjaxResult<Void>> failedOpt = Stream.of(
//                            judgeUpdateDate(old.getQcDate(), request.getQcDate(), now),
//                            judgeUpdateDate(old.getSopDate(), request.getSopDate(), now),
//                            judgeUpdateDate(old.getDeliverDate(), request.getDeliverDate(), now))
//                    .filter(v -> !v.isSuccess()).findAny();
//            if (failedOpt.isPresent()) {
//                return failedOpt.get();
//            }
//
//            flowProcessPlanDAO.lambdaUpdate()
//                    .set(TgFlowProcessPlan::getUpdater, userId)
//                    .set(TgFlowProcessPlan::getQcDate, request.getQcDate())
//                    .set(TgFlowProcessPlan::getSopDate, request.getSopDate())
//                    .set(TgFlowProcessPlan::getDeliverDate, request.getDeliverDate())
//                    .eq(TgFlowProcessPlan::getId, request.getId())
//                    .update();
//        } else {
//            boolean anyPassDate = Stream.of(request.getQcDate(), request.getSopDate(), request.getDeliverDate())
//                    .anyMatch(v -> !v.isAfter(now));
//            if (anyPassDate) {
//                return AjaxResult.error("请填写准确的排期时间，必须满足所有开始时间大于当前时间");
//            }
//
//            TgFlowProcessPlan last = flowProcessPlanDAO.latest();
//            TgFlowProcessPlan plan = new TgFlowProcessPlan();
//            plan.setPeriodDate(last.getPeriodDate().plusMonths(1));
//            plan.fillPeriod();
//
//            plan.setQcDate(request.getQcDate());
//            plan.setSopDate(request.getSopDate());
//            plan.setDeliverDate(request.getDeliverDate());
//            plan.setCreator(userId);
//            plan.setUpdater(userId);
//            plan.setCurrentPeriod(true);
//
//            flowProcessPlanDAO.save(plan);
//            flowProcessPlanDAO.lambdaUpdate()
//                    .set(TgFlowProcessPlan::getCurrentPeriod, false)
//                    .set(TgFlowProcessPlan::getUpdateTime, last.getUpdateTime())
//                    .eq(TgFlowProcessPlan::getId, last.getId())
//                    .update();
//        }
//
//        return AjaxResult.succeed();
//    }

    AjaxResult<Void> judgeUpdateDate(LocalDate old, LocalDate req, LocalDate now) {
        if (!Objects.equals(old, req)) {
            if (!old.isAfter(now)) {
                return AjaxResult.error("请填写准确的排期时间，无法修改已经过去的环节开始时间");
            }
            if (req.equals(now)) {
                return AjaxResult.error("请填写准确的排期时间，无法选择今天");
            }
        }
        return AjaxResult.succeed();
    }

}
