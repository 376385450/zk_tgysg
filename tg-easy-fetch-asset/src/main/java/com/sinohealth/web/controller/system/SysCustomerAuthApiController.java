package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.CustomerAuthListQuery;
import com.sinohealth.system.dto.TgCustomerApplyAuthDto;
import com.sinohealth.system.dto.TgSyncTask;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordBatchRequest;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysCustomerAuthService;
import com.sinohealth.system.service.impl.DefaultSyncHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 客户管理
 */
@Slf4j
@RestController
@RequestMapping("/api/system/customer/auth")
@Api(tags = "客户报表权限管理")
public class SysCustomerAuthApiController extends BaseController {

    @Autowired
    private ISysCustomerAuthService sysCustomerAuthService;

//    @Autowired
//    private TgPgProviderMapper tgPgProviderMapper;

    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private UserDataAssetsService userDataAssetsService;

    @Autowired
    private DefaultSyncHelper defaultSyncHelper;


    @PostMapping("/list")
    @ApiOperation(value = "分配客户列表", response = TgCustomerApplyAuthDto.class)
    public AjaxResult list(@Valid @RequestBody CustomerAuthListQuery query) {
        List<TgCustomerApplyAuthDto> list = sysCustomerAuthService.queryListV2(query.getUserId(), query.getApplyId(), query.getIds());
        return AjaxResult.success(list);
    }


    @PostMapping("/outer/batchUpdateReportForm")
    @ApiOperation("如果报表需要更新, 提交更新任务到 mysql - tg_sync_task_queue")
    public AjaxResult updateReportForm(@RequestBody UpdateRecordBatchRequest request) {
        List<Long> assetsIds = request.getApplyIds();
        if (CollectionUtils.isEmpty(assetsIds)) {
            return AjaxResult.success("请选择更新的项目");
        }

        for (Long assetsId : assetsIds) {
            AjaxResult updateResult = this.updateReportForm(assetsId);
            if (!updateResult.isSuccess()) {
                log.warn("update failed: {}", updateResult);
            }
        }

        return AjaxResult.success(InfoConstants.UPDATE_SYNC_TASK_INFO);
    }


    @GetMapping("/outer/{assetId}/updateReportForm")
    @ApiOperation("如果报表需要更新, 提交更新任务到 mysql - tg_sync_task_queue")
    public AjaxResult updateReportForm(@PathVariable("assetId") Long assetId) {
        // 检查是否需要数据同步
        UserDataAssets assets = new UserDataAssets().selectById(assetId);
        if (Objects.isNull(assets)) {
            return AjaxResult.error("数据不存在: " + assetId);
        }

        boolean needUpdate = userDataAssetsService.getNeedUpdate(assetId, assets.getBaseTableName());
        if (!needUpdate) {
            return AjaxResult.success(InfoConstants.NO_NEED_TO_UPDATE_INFO);
        }

        if (assets.getNeedSyncTag().equals(CommonConstants.NOT_UPDATE_TASK)) {
            // 将任务提交到同步列表
            TgSyncTask tgSyncTask = new TgSyncTask() {{
                setAssetsId(assets.getId());
                setTableName(assets.getAllTableNames());
                setApplySyncTime(DateUtils.getNowDate());
                setSyncState(0);
            }};
            tgSyncTask.insert();
            assets.setNeedSyncTag(CommonConstants.UPDATING_TASK);
            assets.updateById();

            return AjaxResult.success(InfoConstants.UPDATE_SYNC_TASK_INFO);
        } else {
            return AjaxResult.success(InfoConstants.NO_NEED_TO_UPDATE_INFO);
        }

    }


    @ApiOperation(value = "数据资产-分配客户报表列表", response = TgCustomerApplyAuthDto.class)
    @GetMapping("/listForApply")
    public TableDataInfo<TgCustomerApplyAuthDto> queryListForApply(@Validated TgCustomerApplyAuthDto TgCustomerApplyAuthDto) {
        startPage();
        return getDataTable(sysCustomerAuthService.getListForApply(TgCustomerApplyAuthDto));
    }


    @GetMapping("/updateStatus")
    @ApiOperation(value = "更新状态")
    public AjaxResult updateStatus(@RequestParam Integer status, @RequestParam Long id) {
        sysCustomerAuthService.update(Wrappers.<TgCustomerApplyAuth>update().eq("id", id).set("status", status));
        return AjaxResult.success();
    }


}
