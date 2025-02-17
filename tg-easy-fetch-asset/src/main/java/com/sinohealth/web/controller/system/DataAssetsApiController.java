package com.sinohealth.web.controller.system;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.biz.application.dto.request.UserDataAssetsDistRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import com.sinohealth.system.biz.dict.util.ExcelUtil;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.service.DataAssetsService;
import com.sinohealth.system.util.EasyExcelUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/data/assets")
public class DataAssetsApiController extends BaseController {

    @Autowired
    private DataAssetsService dataAssetsService;
    @Autowired
    private AssetsFlowService assetsFlowService;

    @ApiOperation("全局需求管理")
    @PostMapping("/list")
    public AjaxResult<IPage<TgUserDataAssetsDistDto>> queryAssetsDistList(
            @RequestBody @Validated UserDataAssetsDistRequest request) {
        return dataAssetsService.queryAssetsDistList(request);
    }

    @ApiOperation("时间粒度")
    @GetMapping("assetsTimeGra")
    public AjaxResult<List<String>> assetsTimeGra() {
        return dataAssetsService.assetsTimeGra();
    }

    @GetMapping("/queryCron")
    public AjaxResult<String> queryCron(String applicationNo) {
        Map<String, String> cronMap = assetsFlowService.queryApplyFormScheduler();
        return AjaxResult.success("", cronMap.getOrDefault(applicationNo, ""));
    }


    @ApiOperation("导出 全局需求管理")
    @PostMapping("/exportList")
    public void exportAssetsDistList(@RequestBody UserDataAssetsDistRequest request,
                                     HttpServletResponse response) throws Exception {
        request.setPage(1);
        request.setSize(100_000_000);
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream());
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            String[] headers = new String[]{"需求ID", "需求名称", "项目名称", "客户名称", "业务线", "需求类型", "需求性质",
                    "模板名称", "模板类型", "交付周期", "时间粒度", "产品粒度", "申请人", "申请时间", "数据有效期", "需求单状态",
                    "出数人", "预估交付时间", "需求流程状态", "交付方式", "关联表单/工作流", "调度频率", "当前交付期数", "当前版本",
                    "最新版本交付时间", "申请次数", "版本数", "需求个数", "需求份数", "需求成本", "数据条数"};
            WriteSheet sheet = EasyExcel.writerSheet(0, "全局需求管理").head(ExcelUtil.head(headers)).build();

            List<List<Object>> rows = new ArrayList<>();
            AjaxResult<IPage<TgUserDataAssetsDistDto>> pageRes = dataAssetsService.queryAssetsDistList(request);
            List<TgUserDataAssetsDistDto> allList = pageRes.getData().getRecords();

            Map<String, String> cronMap = assetsFlowService.queryApplyFormScheduler();

            for (TgUserDataAssetsDistDto dictDTO : allList) {
                List<Object> row = new ArrayList<>();
                row.add(dictDTO.getApplicationNo());
                row.add(dictDTO.getProjectName());
                row.add(dictDTO.getNewProjectName());
                row.add(dictDTO.getClientNames());
                row.add(BizTypeEnum.getDesc(dictDTO.getBizType()));
                row.add(ApplicationConst.RequireTimeTypeEnum.DESC_MAP.get(dictDTO.getRequireTimeType()));
                row.add(RequireAttrType.DESC_MAP.get(dictDTO.getRequireAttr()));


                row.add(dictDTO.getTemplateName());
                row.add(TemplateTypeEnum.getDesc(dictDTO.getTemplateType()));
                row.add(DeliverTimeTypeEnum.getTypeDesc(dictDTO.getDeliverTimeType()));
                row.add(dictDTO.getTimeGra());
                row.add(dictDTO.getProductGra());
                row.add(dictDTO.getApplicantName());
                row.add(dictDTO.getCreateTime());
                row.add(dictDTO.getDataExpir());
                row.add(ApplyStateEnum.getDesc(dictDTO.getApplyState()));
                row.add(dictDTO.getHandleUser());
                row.add(dictDTO.getExpectDeliveryTime());
                row.add(ApplyRunStateEnum.getDesc(dictDTO.getApplyRunState()));
                row.add(dictDTO.getDataType());
                row.add(dictDTO.getTableName());
                String cronCN = cronMap.getOrDefault(dictDTO.getApplicationNo(), "");
                row.add(cronCN);
                row.add(dictDTO.getPeriod());
                if (Objects.nonNull(dictDTO.getDataVersion()) && dictDTO.getDataVersion() > 0) {
                    row.add("V" + dictDTO.getDataVersion() + "-" + AssetsSnapshotTypeEnum.snapshotMap.get(dictDTO.getSnapshotType()));
                } else {
                    row.add("");
                }
                row.add(dictDTO.getAssetsCreateTime());
                row.add(dictDTO.getApplyCnt());
                row.add(dictDTO.getDataVersion());
                row.add(dictDTO.getDataAmount());
                row.add(dictDTO.getApplyAmount());
                String costMin = Optional.ofNullable(dictDTO.getDataCostMin()).filter(v -> v > 0).map(v -> v + "min").orElse("");
                row.add(costMin);
                row.add(dictDTO.getDataTotal());

                rows.add(row);
            }
            excelWriter.write(rows, sheet);
        } catch (Exception e) {
            log.error("异常", e);
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 更新需求单 状态
     */
    @GetMapping("/markRunState")
    public AjaxResult<Void> markRunState(@RequestParam("applicationNo") String applicationNo,
                                         @RequestParam("state") Integer state) {
        return dataAssetsService.markRunState(applicationNo, state);
    }

    @GetMapping("/updateOwnerId")
    @ApiOperation(value = "更改所有者")
    public AjaxResult updateOwnerId(@RequestParam Long ownerId, @RequestParam Long id) {
        dataAssetsService.updateOwnerId(ownerId, id);
        //dataAssetsService.update(Wrappers.<TgApplicationInfo>update().eq("id", id).set("applicant_id", ownerId));
        return AjaxResult.success();
    }

}
