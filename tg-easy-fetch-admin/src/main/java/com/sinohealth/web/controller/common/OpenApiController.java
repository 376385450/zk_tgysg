package com.sinohealth.web.controller.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.constant.DataAssetsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dataassets.FlowType;
import com.sinohealth.system.biz.alert.dto.AssetsAlertMsg;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.dto.ApplyMetricsDto;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareCallbackRequest;
import com.sinohealth.system.biz.dataassets.dto.request.UserAssetsCallbackRequest;
import com.sinohealth.system.biz.dataassets.helper.AssetsCompareInvoker;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.table.service.TableInfoSnapshotService;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataManageService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.web.controller.system.ApplicationApiController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * OpenApi请求处理
 * <p>
 * 1. 正式环境由于历史原因 回调地址为 http://tgysg.sinohealth.cn api前缀地址 走的 192.168.16.151 nginx 从代理门户过来
 * 1. 测试环境 直接调用 无api前缀路径
 *
 * @author linkaiwei
 * @date 2021/07/22 17:21
 * @since dev
 */
@Api(value = "OpenApi控制器", tags = {"OpenApi管理"})
@Slf4j
@RestController
@RequestMapping({"/api/openapi", "/openapi"})
public class OpenApiController {

    @Value("${openApi.domainName}")
    private String domainName;

    @Autowired
    private AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;

    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private AssetsQcService assetsQcService;
    @Resource
    private ISysUserService userService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private AlertService alertService;
    @Autowired
    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private TableInfoSnapshotService tableInfoSnapshotService;
    @Autowired
    private AssetsCompareInvoker assetsCompareInvoker;

    /**
     * TODO 安全防护
     * Datax完成数据同步后，创建资产数据
     *
     * @see ApplicationApiController#executeWorkFlow(Long)
     */
    @GetMapping("/syncCallback")
    public AjaxResult<Void> syncCallback(@RequestParam(value = "applicationId", required = false) Long applicationId,
                                         @RequestParam(value = "tableName", required = false) String tableName,
                                         @RequestParam(value = "instanceId", required = false) String instanceId,
                                         @RequestParam(value = "triggerId", required = false) String triggerId) {
        try {
            Long finalTriggerId = parseTriggerId(triggerId);
            AjaxResult<Void> createResult;
            try {
                UserAssetsCallbackRequest request = UserAssetsCallbackRequest.builder()
                        .applicationId(applicationId)
                        .tableName(tableName)
                        .instanceId(instanceId)
                        .triggerId(finalTriggerId)
                        .build();
                createResult = userDataAssetsService.createDataAssetsByCallback(request);
            } catch (Exception e) {
                log.error("", e);
                createResult = AjaxResult.error(e.getMessage());
            }

            // 失败告警 & 成功则上传FTP
            if (!createResult.isSuccess()) {
                AssetsAlertMsg msg = AssetsAlertMsg.builder().applyId(applicationId).tableName(tableName).build();
                alertService.sendAssetsAlert(msg);
            } else {
                TgApplicationInfo apply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                        .select(TgApplicationInfo::getAssetsId)
                        .eq(TgApplicationInfo::getId, applicationId));
                if (Objects.nonNull(apply)) {
                    // 同步数据资产数据到ftp服务器
                    userDataAssetsUploadFtpHelper.addFtpTask(apply.getAssetsId());
                }
            }

            // 工作流出数计划 状态更新
            AssetsUpgradeStateEnum state = createResult.isSuccess() ? AssetsUpgradeStateEnum.success : AssetsUpgradeStateEnum.failed;
            assetsFlowBatchDetailDAO.updateState(finalTriggerId, state);

            return createResult;
        } catch (Exception e) {
            log.error("", e);
            AssetsAlertMsg msg = AssetsAlertMsg.builder().applyId(applicationId).tableName(tableName).build();
            alertService.sendAssetsAlert(msg);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 启动工作流时填入的回调地址，必须要返回OK，否则会一直重试
     *
     * @param bizId com.sinohealth.common.enums.dataassets.FlowType
     */
    @GetMapping("/dolphin/callback")
    public String dolphinCallback(@RequestParam(value = "bizId", required = false) String bizId,
                                  @RequestParam(value = "instanceId", required = false) String instanceId,
                                  @RequestParam(value = "state", required = false) Integer state,
                                  @RequestParam(value = "instanceUid", required = false) String uid) {
        if (FlowType.assets_qc.name().equals(bizId)) {
            assetsQcService.dolphinCallBack(uid, state);
        } else {
            userDataAssetsService.dolphinCallBack(uid, state);
        }
        return "OK";
    }

    @GetMapping("/dolphin/rollingTable")
    public AjaxResult<Void> rollingTable(@RequestParam(value = "shardTable", required = false) String shardTable) {
        return tableInfoSnapshotService.rollingTable(shardTable);
    }

    @GetMapping("/dolphin/rollingTablePartly")
    public AjaxResult<Void> rollingTablePartly(@RequestParam(value = "shardTable", required = false) String shardTable) {
        return tableInfoSnapshotService.rollingTablePartly(shardTable);
    }


    @PostMapping("/py/finishAssetsCompare")
    public String finishAssetsCompare(@RequestBody AssetsCompareCallbackRequest req) {
        return assetsCompareInvoker.callbackAssetsCompare(req);
    }

    @PostMapping("/py/finishFileCompare")
    public String finishFileCompare(@RequestBody AssetsCompareCallbackRequest req) {
        return assetsCompareInvoker.callbackFileCompare(req);
    }

    private static Long parseTriggerId(String triggerId) {
        return Optional.ofNullable(triggerId)
                .filter(v -> !Objects.equals(triggerId, "null"))
                .map(v -> {
                    try {
                        long l = Long.parseLong(v);
                        if (Objects.equals(DataAssetsConstants.RE_APPLY_TRIGGER_ID, l)) {
                            return l;
                        }
                        if (l < 1) {
                            return null;
                        }
                        return l;
                    } catch (Exception e) {
//                        log.error("Invalid triggerId:", e);
                        return null;
                    }
                }).orElse(null);
    }

    @GetMapping("/queryMetricsByApply")
    public AjaxResult<List<ApplyMetricsDto>> queryMetricsByApply(@RequestParam(value = "applicationId") Long applicationId) {
        return applicationService.queryMetricsByApply(applicationId);
    }

    /**
     * 获取API接口域名
     *
     * @return API接口域名
     * @author linkaiwei
     * @date 2021-07-27 16:49:45
     * @since 1.1
     */
    //@ApiOperation("获取API接口域名")
    @GetMapping("/getDomainName")
    public AjaxResult<String> getDomainName() {
        return AjaxResult.success("操作成功", domainName);
    }

}
