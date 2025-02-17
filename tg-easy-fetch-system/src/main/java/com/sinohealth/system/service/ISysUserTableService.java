package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.SysUserTable;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author dataplatform
 * @date 2021-04-21
 */
public interface ISysUserTableService extends IService<SysUserTable> {

    public List<SysUserTable> groupByUserTable();

    Long getCountTableByUserId(Long userId);
}
