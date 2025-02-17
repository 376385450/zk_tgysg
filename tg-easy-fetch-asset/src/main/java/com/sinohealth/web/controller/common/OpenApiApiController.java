//package com.sinohealth.web.controller.common;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.sinohealth.api.common.OpenApiApi;
//import com.sinohealth.common.constant.DataAssetsConstants;
//import com.sinohealth.common.core.domain.AjaxResult;
//import com.sinohealth.common.core.domain.entity.SysUser;
//import com.sinohealth.common.enums.HttpMethod;
//import com.sinohealth.common.enums.InvokeFailReasonEnum;
//import com.sinohealth.common.exception.CustomException;
//import com.sinohealth.system.biz.alert.dto.AssetsAlertMsg;
//import com.sinohealth.system.biz.alert.service.AlertService;
//import com.sinohealth.system.biz.application.dto.ApplyMetricsDto;
//import com.sinohealth.system.biz.dataassets.dao.AssetsUpgradeTriggerDAO;
//import com.sinohealth.system.biz.dataassets.dto.request.UserAssetsCallbackRequest;
//import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
//import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
//import com.sinohealth.system.domain.ApiInfoVersion;
//import com.sinohealth.system.domain.ApiInvokeInfo;
//import com.sinohealth.system.domain.TgApplicationInfo;
//import com.sinohealth.system.domain.constant.AsyncTaskConst;
//import com.sinohealth.system.dto.common.OpenApiRequestDTO;
//import com.sinohealth.system.dto.common.OpenApiResponseDTO;
//import com.sinohealth.system.mapper.ApiInvokeInfoMapper;
//import com.sinohealth.system.mapper.TgApplicationInfoMapper;
//import com.sinohealth.system.service.IApiInfoVersionService;
//import com.sinohealth.system.service.IApiInvokeInfoService;
//import com.sinohealth.system.service.IApplicationService;
//import com.sinohealth.system.service.ISysUserService;
//import io.swagger.annotations.Api;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.util.ObjectUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
///**
// * OpenApi请求处理
// *
// * @author linkaiwei
// * @date 2021/07/22 17:21
// * @since dev
// */
//@Api(value = "OpenApi控制器", tags = {"OpenApi管理"})
//@Slf4j
//@RestController
//@RequestMapping({"/api/openapi", "/openapi"})
//public class OpenApiApiController implements OpenApiApi {
//
//    @Value("${openApi.domainName}")
//    private String domainName;
//
//    @Resource
//    private ApiInvokeInfoMapper apiInvokeInfoMapper;
//    @Autowired
//    private AssetsUpgradeTriggerDAO assetsUpgradeTriggerDAO;
//    @Autowired
//    private TgApplicationInfoMapper applicationInfoMapper;
//
//    @Autowired
//    private UserDataAssetsService userDataAssetsService;
//    @Resource
//    private IApiInfoVersionService apiInfoVersionService;
//    @Resource
//    private IApiInvokeInfoService apiInvokeInfoService;
//    @Resource
//    private ISysUserService userService;
//    @Autowired
//    private IApplicationService applicationService;
//    @Autowired
//    private AlertService alertService;
//    @Autowired
//    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
//
//    /**
//     * TODO 安全防护
//     * Datax完成数据同步后，创建资产数据
//     *
//     * @see ApplicationController#executeWorkFlow
//     */
//    @GetMapping("/syncCallback")
//    public AjaxResult<Void> syncCallback(@RequestParam(value = "applicationId", required = false) Long applicationId,
//                                         @RequestParam(value = "tableName", required = false) String tableName,
//                                         @RequestParam(value = "instanceId", required = false) String instanceId,
//                                         @RequestParam(value = "triggerId", required = false) String triggerId) {
//        try {
//            Long finalTriggerId = parseTriggerId(triggerId);
//            AjaxResult<Void> createResult;
//            try {
//                UserAssetsCallbackRequest request = UserAssetsCallbackRequest.builder()
//                        .applicationId(applicationId)
//                        .tableName(tableName)
//                        .instanceId(instanceId)
//                        .triggerId(finalTriggerId)
//                        .build();
//                createResult = userDataAssetsService.createDataAssetsByCallback(request);
//            } catch (Exception e) {
//                log.error("", e);
//                createResult = AjaxResult.error(e.getMessage());
//            }
//
//            if (!createResult.isSuccess()) {
//                AssetsAlertMsg msg = AssetsAlertMsg.builder().applyId(applicationId).build();
//                alertService.sendAssetsAlert(msg);
//            } else {
//
//                TgApplicationInfo apply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
//                        .select(TgApplicationInfo::getAssetsId)
//                        .eq(TgApplicationInfo::getId, applicationId));
//                if (Objects.nonNull(apply)) {
//                    // 同步数据资产数据到ftp服务器
//                    userDataAssetsUploadFtpHelper.uploadFtp(apply.getAssetsId());
//                }
//            }
//
//
//            int state = createResult.isSuccess() ? AsyncTaskConst.Status.SUCCEED : AsyncTaskConst.Status.FAILED;
//            assetsUpgradeTriggerDAO.updateState(applicationId, state);
//
//            return createResult;
//        } catch (Exception e) {
//            log.error("", e);
//            AssetsAlertMsg msg = AssetsAlertMsg.builder().applyId(applicationId).build();
//            alertService.sendAssetsAlert(msg);
//            return AjaxResult.error(e.getMessage());
//        }
//    }
//
//    private static Long parseTriggerId(String triggerId) {
//        return Optional.ofNullable(triggerId)
//                .filter(v -> !Objects.equals(triggerId, "null"))
//                .map(v -> {
//                    try {
//                        long l = Long.parseLong(v);
//                        if (Objects.equals(DataAssetsConstants.RE_APPLY_TRIGGER_ID, l)) {
//                            return l;
//                        }
//                        if (l < 1) {
//                            return null;
//                        }
//                        return l;
//                    } catch (Exception e) {
//                        log.error("Invalid triggerId:", e);
//                        return null;
//                    }
//                }).orElse(null);
//    }
//
//    @GetMapping("/queryMetricsByApply")
//    public AjaxResult<List<ApplyMetricsDto>> queryMetricsByApply(@RequestParam(value = "applicationId") Long applicationId) {
//        return applicationService.queryMetricsByApply(applicationId);
//    }
//
//    /**
//     * 获取API接口域名
//     *
//     * @return API接口域名
//     * @author linkaiwei
//     * @date 2021-07-27 16:49:45
//     * @since 1.1
//     */
//    //@ApiOperation("获取API接口域名")
//    @Override
//    @GetMapping("/getDomainName")
//    public AjaxResult<String> getDomainName() {
//        return AjaxResult.success("操作成功", domainName);
//    }
//
//
//    /**
//     * openApi调用
//     *
//     * @param request
//     * @param response
//     * @param page
//     * @param size
//     * @param token
//     * @param requestDTO
//     * @return 返回参数
//     * @author linkaiwei
//     * @date 2021-07-22 17:41:51
//     * @since dev
//     */
//    @Override
//    @RequestMapping("/**")
//    public OpenApiResponseDTO openApi(HttpServletRequest request, HttpServletResponse response,
//                                      @RequestParam(value = "page", required = false) Long page,
//                                      @RequestParam(value = "size", required = false) Long size,
//                                      @RequestParam(value = "token", required = false) String token,
//                                      @RequestBody(required = false) OpenApiRequestDTO requestDTO) {
//        // 获取开始时间
//        long startTime = System.currentTimeMillis();
//        Long id = null;
//
//        try {
//            final String method = request.getMethod();
//            final String requestUrl = request.getRequestURL().toString();
//            String url = requestUrl.substring(requestUrl.indexOf("openapi/") + 8);
//
//            // 验证api
//            final ApiInfoVersion apiInfoVersion = apiInfoVersionService.getApiInfoVersion(url, method);
//            if (apiInfoVersion == null) {
//                // 404
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                return null;
//            }
//            id = apiInfoVersion.getId();
//
//            // 验证请求参数
//            if (HttpMethod.POST.matches(method.toUpperCase())) {
//                if (ObjectUtils.isEmpty(requestDTO)) {
//                    throw new CustomException("请求参数有误");
//                }
//                page = requestDTO.getPage();
//                size = requestDTO.getSize();
//                token = requestDTO.getToken();
//            }
//            if (page == null || size == null || StringUtils.isBlank(token)) {
//                throw new CustomException("请求参数有误");
//            }
//
//            // 验证token
//            final SysUser sysUser = userService.getUserByToken(token);
//            if (sysUser == null) {
//                throw new CustomException("token无效");
//            }
//
//            // 调用接口
//            return apiInvokeInfoService.invokeApi(id + "",
//                    JSON.toJSONString(new JSONObject().fluentPut("pageNum", page).fluentPut("pageSize", size)),
//                    4, sysUser.getUserId(), sysUser.getUserName());
//
//        } catch (Exception e) {
//            log.error("openApi >> 调用异常", e);
//
//            // API调用记录
//            ApiInvokeInfo apiInvokeInfo = new ApiInvokeInfo();
//            apiInvokeInfo.setApiVersionId(id + "");
//            apiInvokeInfo.setRequestParam(new JSONObject()
//                    .fluentPut("page", page)
//                    .fluentPut("size", size).toJSONString());
//            apiInvokeInfo.setInvokeType("1");
//            // 用户ID为0，创建人和更新人姓名都为空字符串，表示系统自动记录
//            apiInvokeInfo.setCreateId(0L);
//            apiInvokeInfo.setCreateBy("");
//            apiInvokeInfo.setInvokeStatus("0");
//            apiInvokeInfo.setInvokeCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR + "");
//
//            if (e instanceof CustomException) {
//                CustomException exception = (CustomException) e;
//                // 插入调用记录
//                if (id != null) {
//                    apiInvokeInfo.setInvokeFailReason(InvokeFailReasonEnum.WRONG_INPUT_PARAMETERS.getId());
//                    apiInvokeInfo.setInvokeMessage(exception.getMessage());
//                    // 获取结束时间
//                    long endTime = System.currentTimeMillis();
//                    long excuteTime = endTime - startTime;
//                    apiInvokeInfo.setExecuteTime(String.valueOf(excuteTime));
//                    apiInvokeInfoMapper.insert(apiInvokeInfo);
//                }
//
//                throw e;
//            }
//
//            apiInvokeInfo.setInvokeFailReason(InvokeFailReasonEnum.SERVER_EXCEPTION.getId());
//            apiInvokeInfo.setInvokeMessage(e.getMessage());
//            // 获取结束时间
//            long endTime = System.currentTimeMillis();
//            long excuteTime = endTime - startTime;
//            apiInvokeInfo.setExecuteTime(String.valueOf(excuteTime));
//            apiInvokeInfoMapper.insert(apiInvokeInfo);
//            return OpenApiResponseDTO.error("调用失败", null);
//        }
//    }
//
//}
