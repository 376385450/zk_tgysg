package com.sinohealth.api.tablemanage;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.domain.TableInfoDiy;
import com.sinohealth.system.domain.vo.DiyTableUpdateResult;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/relationTableManage")
public interface RelationTableManageApi {

    @GetMapping("/list")
    TableDataInfo<TableInfoDiy> list(
            @ApiParam("页码，默认1") @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @ApiParam("每页数量，默认10") @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @ApiParam("搜索名称") @RequestParam(value = "name", required = false) String name);

    @PostMapping("/importData")
    AjaxResult<DiyTableUpdateResult> importDataset(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "tableName", required = false) String tableName,
                                                   @RequestParam(value = "tableId", required = false) Long tableId,
                                                   @RequestParam(value = "ignoreNotice", required = false) Boolean ignoreNotice
    );
}
