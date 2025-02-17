package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TableStatistics;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author dataplatform
 * @date 2021-05-07
 */
public interface ITableStatisticsService extends IService<TableStatistics> {

    public List<TableStatistics> ininTableStatistics();

}
