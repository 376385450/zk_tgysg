package com.sinohealth.system.biz.process.facade;

import com.alibaba.fastjson.JSONObject;
import com.sinohealth.common.enums.TableInfoSnapshotCompareState;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.process.FlowProcessAlertCategory;
import com.sinohealth.common.enums.process.FlowProcessTaskEnum;
import com.sinohealth.common.enums.process.FlowProcessUpdateType;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.process.dao.TgFlowProcessAlertConfigDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessAlertConfig;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.dto.FlowProcessAlertMessageRequest;
import com.sinohealth.system.biz.process.service.TgFlowProcessManagementService;
import com.sinohealth.system.biz.process.vo.FlowProcessAttachVO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompare;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class TgFlowProcessAlertFacade {

    private final TgFlowProcessAlertConfigDAO tgFlowProcessAlertConfigDAO;

    private final TgFlowProcessManagementService tgFlowProcessManagementService;

    private final AlertService alertService;

    /**
     * 发送全流程告警
     *
     * @param management 全流程记录
     * @param code       主流程编码
     * @param isSuccess  失败告警还是成功告警
     */
    public void sendFlowProcessMsg(TgFlowProcessManagement management, String code, boolean isSuccess) {
        sendMsg(alertMap(management, false), FlowProcessAlertCategory.FLOW_PROCESS.getCode(), code, isSuccess);
    }

    /**
     * 发送全流程子流程告警
     *
     * @param management 全流程记录
     * @param code       子流程编码
     * @param isSuccess  失败告警还是成功告警
     */
    public void sendSubMsg(TgFlowProcessManagement management, String code, boolean isSuccess) {
        sendMsg(alertMap(management, true), FlowProcessAlertCategory.SUB_PROCESS.getCode(), code, isSuccess);
    }

    /**
     * 发送工作流推数告警
     *
     * @param assetsFlowBatch 工作流推数信息
     */
    public void sendAssetsFlowAlert(AssetsFlowBatch assetsFlowBatch) {
        boolean isSuccess = Objects.equals(AssetsUpgradeStateEnum.success.name(), assetsFlowBatch.getState());

        // 组装map信息【替换信息】
        FlowProcessAlertMessageRequest request = new FlowProcessAlertMessageRequest();
        request.setPeriod(assetsFlowBatch.getPeriod());
        request.setVersionCategory(assetsFlowBatch.getFlowProcessType());
        if (Objects.isNull(assetsFlowBatch.getBizId())) {
            request.setManageName(assetsFlowBatch.getName());
            request.setProductCodes("");
        } else {
            TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(assetsFlowBatch.getBizId());
            request.setManageName(management.getName());
            request.setProductCodes("");
        }
        sendMsg(alertMap(request), FlowProcessAlertCategory.SUB_PROCESS.getCode(), FlowProcessTaskEnum.WORK_FLOW.getCode(), isSuccess);
    }

    /**
     * 发送资产升级告警
     *
     * @param tableInfoSnapshot 表快照信息【最新版本】
     */
    public void sendAssetsUpGradeAlert(TableInfoSnapshot tableInfoSnapshot) {
        boolean isSuccess = Objects.equals(AssetsUpgradeStateEnum.success.name(), tableInfoSnapshot.getPushStatus());

        // 组装map信息【替换信息】
        FlowProcessAlertMessageRequest request = new FlowProcessAlertMessageRequest();
        request.setPeriod(tableInfoSnapshot.getVersionPeriod());
        request.setVersionCategory(tableInfoSnapshot.getFlowProcessType());
        String prodCodes = StringUtils.isEmpty(tableInfoSnapshot.getProdCodes()) ? "全品类" :
                tableInfoSnapshot.getProdCodes();
        request.setProductCodes(prodCodes);
        if (Objects.isNull(tableInfoSnapshot.getBizId())) {
            request.setManageName("");
        } else {
            TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(tableInfoSnapshot.getBizId());
            request.setManageName(management.getName());
        }
        sendMsg(alertMap(request), FlowProcessAlertCategory.SUB_PROCESS.getCode(), FlowProcessTaskEnum.SYNC.getCode(),
                isSuccess);
    }

    /**
     * 发送底表对比【数据】告警
     *
     * @param compare 比对任务信息
     */
    public void sendDataCompareAlert(TgTableInfoSnapshotCompare compare) {
        boolean isSuccess = Objects.equals(TableInfoSnapshotCompareState.COMPLETED.getType(),
                compare.getState());

        // 组装map信息【替换信息】
        FlowProcessAlertMessageRequest request = new FlowProcessAlertMessageRequest();
        if (Objects.isNull(compare.getBizId())) {
            request.setManageName("");
            request.setProductCodes("");
            request.setPeriod("");
            request.setVersionCategory("");
        } else {
            TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(compare.getBizId());
            request.setManageName(management.getName());
            request.setProductCodes("");
            request.setPeriod(management.getPeriod());
            request.setVersionCategory(management.getVersionCategory());
        }
        sendMsg(alertMap(request), FlowProcessAlertCategory.SUB_PROCESS.getCode(), FlowProcessTaskEnum.TABLE_DATA_COMPARE.getCode(),
                isSuccess);
    }

    /**
     * 发送qc告警
     *
     * @param qcBatch qc任务信息
     */
    public void sendAssetsQcBatchAlert(AssetsQcBatch qcBatch) {
        boolean isSuccess = Objects.equals(AssetsUpgradeStateEnum.success.name(), qcBatch.getState());

        // 组装map信息【替换信息】
        FlowProcessAlertMessageRequest request = new FlowProcessAlertMessageRequest();
        if (Objects.isNull(qcBatch.getBizId())) {
            request.setManageName("");
            request.setProductCodes("");
            request.setPeriod("");
            request.setVersionCategory("");
        } else {
            TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(qcBatch.getBizId());
            request.setManageName(management.getName());
            request.setProductCodes("");
            request.setPeriod(management.getPeriod());
            request.setVersionCategory(management.getVersionCategory());
        }
        sendMsg(alertMap(request), FlowProcessAlertCategory.SUB_PROCESS.getCode(), FlowProcessTaskEnum.QC.getCode(),
                isSuccess);
    }

    /**
     * 发送powerBi告警
     *
     * @param batch powerBi推数任务信息
     */
    public void sendPowerBiAlert(PowerBiPushBatch batch) {
        boolean isSuccess = Objects.equals(AssetsUpgradeStateEnum.success.name(), batch.getState());

        // 组装map信息【替换信息】
        FlowProcessAlertMessageRequest request = new FlowProcessAlertMessageRequest();
        if (Objects.isNull(batch.getBizId())) {
            request.setManageName(batch.getName());
            request.setProductCodes("");
            request.setPeriod("");
            request.setVersionCategory("");
        } else {
            TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(batch.getBizId());
            request.setManageName(management.getName());
            request.setProductCodes("");
            request.setPeriod(management.getPeriod());
            request.setVersionCategory(management.getVersionCategory());
        }
        sendMsg(alertMap(request), FlowProcessAlertCategory.SUB_PROCESS.getCode(), FlowProcessTaskEnum.PUSH_POWER_BI.getCode(),
                isSuccess);
    }

    /**
     * 发送告警内容
     *
     * @param params    告警内容替换符映射关系表
     * @param category  告警类型
     * @param code      编码
     * @param isSuccess 是否发送成功告警
     */
    private void sendMsg(Map<String, String> params, String category, String code, boolean isSuccess) {
        TgFlowProcessAlertConfig config = tgFlowProcessAlertConfigDAO.query(category, code);
        if (Objects.isNull(config)) {
            log.info("告警配置为空：category-{}, code-{}", category, code);
            return;
        }
        String members;
        String title;
        String content;
        String webHook;
        if (isSuccess && config.getSuccessAlertSwitch()) {
            members = config.getSuccessMemberNumbers();
            title = config.getSuccessAlertTitle();
            content = config.getSuccessAlertContent();
            webHook = config.getSuccessWebHook();
        } else if (config.getFailAlertSwitch()) {
            members = config.getFailMemberNumbers();
            title = config.getFailAlertTitle();
            content = config.getFailAlertContent();
            webHook = config.getFailWebHook();
        } else {
            log.info("任务告警配置未开：category-{}, code-{},任务状态-{}", category, code, isSuccess);
            return;
        }
        // 替换信息
        title = replareAlert(params, title);
        content = replareAlert(params, content);
        // 发送告警
        alertService.sendFlowProcessAlert(webHook, members, title, content);
    }

    /**
     * 封装替换信息
     *
     * @param request 请求信息
     * @return 替换信息
     */
    private Map<String, String> alertMap(FlowProcessAlertMessageRequest request) {
        Map<String, String> r = new HashMap<>();
        r.put("\\$\\{期数\\}", StringUtils.isEmpty(request.getPeriod()) ? "" : request.getPeriod());
        r.put("\\$\\{版本类型\\}",
                StringUtils.isEmpty(request.getVersionCategory()) ? "" :
                        FlowProcessTypeEnum.getDescByName(request.getVersionCategory()));
        r.put("\\$\\{流程名称\\}", StringUtils.isEmpty(request.getManageName()) ? "" : request.getManageName());
        r.put("\\$\\{关联品类\\}", StringUtils.isEmpty(request.getProductCodes()) ? "" : request.getProductCodes());
        return r;
    }

    /**
     * 构建告警替换文本
     *
     * @param management 管理记录
     * @return 需替换内容
     */
    private Map<String, String> alertMap(TgFlowProcessManagement management, boolean missProduct) {
        Map<String, String> r = new HashMap<>();
        r.put("\\$\\{期数\\}", management.getPeriod());
        r.put("\\$\\{版本类型\\}", FlowProcessTypeEnum.getDescByName(management.getVersionCategory()));
        r.put("\\$\\{流程名称\\}", management.getName());
        if (missProduct) {
            r.put("\\$\\{关联品类\\}", "");
        } else {
            String product = getProcodesByManage(management);
            r.put("\\$\\{关联品类\\}", product);
        }

        return r;
    }

    /**
     * 根据全流程构建 品类信息
     *
     * @param management 全流程管理任务信息
     * @return 品类信息
     */
    private String getProcodesByManage(TgFlowProcessManagement management) {
        // 构建品类信息
        FlowProcessAttachVO attach = getAttachVO(management);
        String product = null;
        if (Objects.equals(attach.getDetail().getUpdateType(), FlowProcessUpdateType.ALL.getCode()) || CollectionUtils.isEmpty(attach.getProdCodes())) {
            // 全量
            product = "全品类";
        } else {
            // 固定品类
            product = String.join(",", attach.getProdCodes());
        }
        return product;
    }

    /**
     * 替换告警内容
     *
     * @param parmas  参数
     * @param content 文本
     * @return 替换后内容
     */
    private String replareAlert(Map<String, String> parmas, String content) {
        for (Map.Entry<String, String> entry : parmas.entrySet()) {
            content = content.replaceAll(entry.getKey(), entry.getValue());
        }
        return content;
    }

    /**
     * 获取附加信息
     *
     * @param i 实体
     * @return 附加信息
     */
    private FlowProcessAttachVO getAttachVO(TgFlowProcessManagement i) {
        return JSONObject.parseObject(i.getAttach(), FlowProcessAttachVO.class);
    }
}
