package com.sinohealth.system.biz.transfer.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 解析提数申请： 工作流模板，自定义模板
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-03-08 17:49
 */

@Slf4j
public abstract class CrApplyListener extends AbsApplyListener<CrApplyVO> {

    public CrApplyListener(HttpServletRequest req,
                           DataSourceTransactionManager dataSourceTransactionManager,
                           TransactionDefinition transactionDefinition, Validator validator) {
        super(dataSourceTransactionManager, transactionDefinition, validator, req);
    }

//    @Override
//    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        if (Objects.equals(excelType, ImportExcelType.FLOW)) {
//            handleTransaction(() -> this.saveCommonApply(this::handleFlowApplyDetail, RedisKeys.FlowApply.TRANS_APPLY_MAP));
//        } else if (Objects.equals(excelType, ImportExcelType.CUSTOM)) {
//            handleTransaction(() -> this.saveCommonApply(this::handleCustomApplyDetail, RedisKeys.CustomApply.TRANS_APPLY_MAP));
//        }
//        list.clear();
//    }


    /**
     * 注意： 一次处理完全部，为了顺序映射问题
     *
     * @param applyKey excelId -> applyId
     */
    public void saveCommonApply(ExcelFlowApplyHandler handler, String applyKey) {

        ApplyCheckCtx ctx = preCheck();
        if (Objects.isNull(ctx)) {
            return;
        }

        List<Integer> excelIds = ctx.getExcelIds();
        List<Integer> ignoreIds = ctx.getIgnoreIds();
        boolean debug = ctx.isDebug();

        Set<String> realNames = Lambda.buildSet(list, CrApplyVO::getApplicant);
        Map<String, Long> userMap = userService.selectUserByRealNames(realNames);

        List<String> projectNames = Lambda.buildList(list, CrApplyVO::getProject);
        List<Project> proList = projectMapper.selectList(new QueryWrapper<Project>().lambda().in(Project::getName, projectNames));
        Map<String, Project> proMap = Lambda.buildMap(proList, Project::getName);

        AtomicInteger cnt = new AtomicInteger();
        Map<Long, Set<Long>> proUsers = new HashMap<>();
        int rowId = 0;
        for (CrApplyVO vo : list) {
            rowId++;
            if (CollectionUtils.isNotEmpty(excelIds) && !excelIds.contains(rowId)) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(ignoreIds) && ignoreIds.contains(rowId)) {
                continue;
            }

            log.warn("no:{} handle: {} {}", cnt.incrementAndGet(), rowId, vo.getProjectName());
            TgTemplateInfo template = queryTemplate(vo.getTemplateName());
            TgAssetInfo asset = queryAssets(template.getId());

            TgApplicationInfo info = TgApplicationInfo.newInstance();
            info.setTemplateId(template.getId());
            info.setNewAssetId(asset.getId());
            info.setProjectId(Optional.ofNullable(proMap.get(vo.getProject())).map(Project::getId).orElseThrow(() -> new CustomException("项目不存在: " + vo.getProject())));

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
            info.setColsInfo(template.getColsInfo());
            info.setBaseTableId(template.getBaseTableId());
            info.setBaseTableName(template.getBaseTableName());
            info.setProcessId(template.getProcessId());
            info.setExportProjectName(true);
            // 过期时间直接取表格值
            info.setDataExpir(vo.getDataExpire());

            handler.handleRowDetail(vo, template, info);

            info.setRequireAttr(RequireAttrType.getByTransferDesc(vo.getRequireAttr()));
            info.setContractNo(Optional.ofNullable(vo.getContractNo()).filter(v -> StringUtils.isNotBlank(v) && !v.contains("NULL")).orElse(null));

            if (BooleanUtils.isTrue(transferProperties.getCloseRound())) {
                MDC.put(CommonConstants.REMOVE_ROUND, "RRRRRemove");
            }
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
