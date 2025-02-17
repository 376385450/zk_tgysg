package com.sinohealth.web.controller.personalservice;

import com.github.pagehelper.PageInfo;
import com.sinohealth.api.personalservice.PersonalServiceApi;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.dto.personalservice.JudgePermissionRequest;
import com.sinohealth.system.dto.personalservice.PageQueryServiceRequest;
import com.sinohealth.system.dto.personalservice.QueryPermissionListRequest;
import com.sinohealth.system.service.IPersonalService;
import com.sinohealth.system.vo.PersonalServiceVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 10:31
 */
@RestController
@Api(tags = "我的服务")
@Slf4j
@RequestMapping({"/api/personal_service"})
public class PersonalServiceApiController extends BaseController implements PersonalServiceApi {

    @Resource
    private IPersonalService personalService;
    @Autowired
    private IntegrateSyncProcessDefService integrateSyncProcessDefService;

    @ApiOperation("分页查询我的服务")
    @PostMapping("/pageQuery")
    public AjaxResult<PageInfo<PersonalServiceVo>> pageQuery(@RequestBody @Validated PageQueryServiceRequest pageRequest) {
        return personalService.pageQuery(pageRequest);
    }

    @ApiOperation("根据资产查询权限")
    @PostMapping("/queryPermission")
    public AjaxResult<List<String>> queryPermissionList(@RequestBody @Validated QueryPermissionListRequest queryPermissionListRequest) {
        return personalService.queryPermissionList(queryPermissionListRequest.getAssetId());
    }

    @ApiOperation("判断是否有操作权限")
    @PostMapping("/judgePermission")
    public AjaxResult<Object> judgePermission(@RequestBody @Validated JudgePermissionRequest judgePermissionRequest) {
        return personalService.judgePermission(judgePermissionRequest);
    }

    @ApiOperation("查看实例")
    @GetMapping("/queryInstance")
    public AjaxResult queryInstance(@RequestParam(value = "flowId") Integer flowId,
                                    @RequestParam(value = "state", required = false) Integer state,
                                    @RequestParam("pageNo") Integer pageNo,
                                    @RequestParam("pageSize") Integer pageSize) {
        return integrateSyncProcessDefService.wrapException(() ->
                AjaxResult.success(integrateSyncProcessDefService.querySyncDetail(flowId, state, pageNo, pageSize)));
    }

}
