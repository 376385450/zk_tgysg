package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.transfer.dto.CrInCompleteCustomApplyVO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.MDC;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kuangcp
 * 2024-07-22 14:00
 */
@Slf4j
public class CrInCompleteCustomListener extends AbsApplyListener<CrInCompleteCustomApplyVO> {

    final List<CrInCompleteCustomApplyVO> list = new ArrayList<>();

    public CrInCompleteCustomListener(DataSourceTransactionManager dataSourceTransactionManager, TransactionDefinition transactionDefinition, Validator validator, HttpServletRequest req) {
        super(dataSourceTransactionManager, transactionDefinition, validator, req);
    }

    /**
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context analysis context
     */
    @Override
    public void invoke(CrInCompleteCustomApplyVO data, AnalysisContext context) {
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        handleTransaction(() -> {
            saveCommonApply(RedisKeys.InCompleteCustomApply.TRANS_APPLY_MAP);
        });
        list.clear();
    }


    /**
     * @param applyKey excelId -> applyId
     * @see CrApplyListener#saveCommonApply(ExcelFlowApplyHandler, String)
     */
    public void saveCommonApply(String applyKey) {
        ApplyCheckCtx ctx = preCheck();
        if (Objects.isNull(ctx)) {
            return;
        }

        List<Integer> excelIds = ctx.getExcelIds();
        List<Integer> ignoreIds = ctx.getIgnoreIds();
        boolean debug = ctx.isDebug();

        Set<String> realNames = Lambda.buildSet(list, CrInCompleteCustomApplyVO::getApplicant);
        Map<String, Long> userMap = userService.selectUserByRealNames(realNames);

        List<String> projectNames = Lambda.buildList(list, CrInCompleteCustomApplyVO::getProject);
        List<Project> proList = projectMapper.selectList(new QueryWrapper<Project>().lambda().in(Project::getName, projectNames));
        Map<String, Project> proMap = Lambda.buildMap(proList, Project::getName);

        AtomicInteger cnt = new AtomicInteger();
        Map<Long, Set<Long>> proUsers = new HashMap<>();
        int rowId = 0;
        for (CrInCompleteCustomApplyVO vo : list) {
            rowId++;
            if (CollectionUtils.isNotEmpty(excelIds) && !excelIds.contains(rowId)) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(ignoreIds) && ignoreIds.contains(rowId)) {
                continue;
            }

            log.warn("no:{} handle: {} {}", cnt.incrementAndGet(), rowId, vo.getProjectName());
            TgTemplateInfo template = this.queryTemplate(vo.getTemplateName());
            TgAssetInfo asset = queryAssets(template.getId());

            TgApplicationInfo info = TgApplicationInfo.newInstance();
            info.setTemplateId(template.getId());
            info.setNewAssetId(asset.getId());
            info.setProjectId(Optional.ofNullable(proMap.get(vo.getProject())).map(Project::getId).orElse(null));

//            info.setTemplateVersion(template.getVersion());
            info.setRequireTimeType(ApplicationConst.RequireTimeType.PERSISTENCE);
            info.setProjectName(vo.getProjectName());

            // 重新申请
            TgApplicationInfo exist = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                    .select(TgApplicationInfo::getId, TgApplicationInfo::getAssetsId)
                    .eq(TgApplicationInfo::getProjectName, vo.getProjectName())
                    .orderByDesc(TgApplicationInfo::getId)
                    .last(" limit 1"));
            if (Objects.nonNull(exist)) {
                log.info("reApply {}", exist.getId());
                info.setId(exist.getId());
                info.setAssetsId(exist.getAssetsId());
            }

            Long applyId = BooleanUtils.isTrue(transferProperties.getMockSameUser())
                    ? transferProperties.getApplicantId() : userMap.get(vo.getApplicant());
            fillUserAuthById(req, applyId);
            info.setApplicantId(applyId);
            TgCollectionUtils.appendSetVal(proUsers, info.getProjectId(), info.getApplicantId());

            info.setDeliverTimeType(DeliverTimeTypeEnum.getTypeByTransferDesc(vo.getDeliverTimeType()));
            try {
                Optional.ofNullable(vo.getDeliverDelay()).map(Integer::parseInt).ifPresent(info::setDeliverDelay);
            } catch (Exception e) {
                log.error("", e);
            }
//            info.setColsInfo(template.getColsInfo());
//            info.setBaseTableId(template.getBaseTableId());
//            info.setBaseTableName(template.getBaseTableName());
            info.setProcessId(template.getProcessId());
            info.setExportProjectName(true);
            // 使用原始字段顺序
            info.setRelateDict(false);

            info.setDataExpir(vo.getDataExpire());
            info.setRequireAttr(RequireAttrType.getByTransferDesc(vo.getRequireAttr()));
            info.setContractNo(Optional.ofNullable(vo.getContractNo()).filter(v -> StringUtils.isNotBlank(v) && !v.contains("NULL")).orElse(null));

            super.fillFlowId(vo.getFlowName(), template, info);

            if (BooleanUtils.isTrue(transferProperties.getCloseRound())) {
                MDC.put(CommonConstants.REMOVE_ROUND, "RRRRRemove");
            }
            // 保留工作流设置
            MDC.put(CommonConstants.TRANSFER, "test");

            AjaxResult tmp;
            boolean tryApply = BooleanUtils.isTrue(debug);
            if (tryApply) {
                tmp = applicationService.tryApplication(info);
                log.info("try: tmp={}", tmp);
            } else {
                tmp = applicationService.addTemplateApplication(info);
                log.info("create: tmp={}", tmp);
            }
            if (tmp.isSuccess() && !tryApply) {
                int finalRow = rowId;
                Optional<Long> newApplyIdOpt = Optional.of(tmp).map(AjaxResult::getData).map(v -> {
                    if (v instanceof TgApplicationInfo) {
                        return (TgApplicationInfo) v;
                    }
                    log.warn("type not match: v={}", v);
                    return null;
                }).map(TgApplicationInfo::getId);
                if (newApplyIdOpt.isPresent() && Objects.nonNull(exist)) {
                    // 未审核通过就重新申请，不会创建新申请单
                    if (!Objects.equals(exist.getId(), newApplyIdOpt.get())) {
                        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                                .eq(TgApplicationInfo::getId, exist.getId()));
                    }
                }
                newApplyIdOpt.ifPresent(v -> redisTemplate.<Long, Long>opsForHash().put(applyKey, finalRow, v));
            } else {
                log.warn("{} tmp={}", vo.getProjectName(), tmp);
                if (!tryApply) {
                    log.error("提申请失败", new RuntimeException("提申请失败 " + vo.getProjectName()));
                }
            }
        }

        projectService.patchUserProjectRelation(proUsers);

    }
}
