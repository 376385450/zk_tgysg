package com.sinohealth.web.controller.dataasset.assetManage;//package com.sinohealth.web.controller.data_asset.assetManage;

import com.sinohealth.api.dataasset.TagManageApi;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author shallwetalk
 * @Date 2023/7/27
 */
@Api("资产标签")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/assetManage/tag"})
public class TagManageApiController implements TagManageApi {

    @Override
    @Operation(summary = "标签分页")
    @PostMapping("/page")
    public void tagPage() {

    }


    @Override
    @Operation(summary = "新增或编辑")
    @PostMapping("/saveOrUpdate")
    public void saveOrUpdate() {

    }

    @Override
    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public void delete(@RequestParam("id") Integer id) {

    }

}
