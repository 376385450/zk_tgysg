package com.sinohealth.web.controller.dataasset.assetManage;//package com.sinohealth.web.controller.data_asset.assetManage;

import com.sinohealth.api.dataasset.ModuleManageApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author shallwetalk
 * @Date 2023/7/27
 */
@Api("模型管理")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/assetManage/module"})
public class ModuleManageApiController implements ModuleManageApi {



}
