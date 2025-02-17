package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TableInfoDiy;
import com.sinohealth.system.domain.vo.DiyTableUpdateResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 关联表管理Service接口
 *
 */
public interface RelationTableManageService   extends IService<TableInfoDiy> {


    List<TableInfoDiy> page( Integer pageNum, Integer pageSize, String name);

    AjaxResult<DiyTableUpdateResult> importData(MultipartFile file, String tableName, Long tableId, Boolean ignoreNotice);

}
