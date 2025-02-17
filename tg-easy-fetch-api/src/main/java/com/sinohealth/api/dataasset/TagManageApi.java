package com.sinohealth.api.dataasset;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data_asset/assetManage/tag")
public interface TagManageApi {

    @Operation(summary = "标签分页")
    @PostMapping("/page")
    void tagPage();


    @Operation(summary = "新增或编辑")
    @PostMapping("/saveOrUpdate")
    void saveOrUpdate();

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    void delete(@RequestParam("id") Integer id);
}
