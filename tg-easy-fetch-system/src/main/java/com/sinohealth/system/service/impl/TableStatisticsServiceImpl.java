package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.domain.TableStatistics;
import com.sinohealth.system.mapper.TableStatisticsMapper;
import com.sinohealth.system.service.ITableStatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author dataplatform
 * @date 2021-05-07
 */
@Service
public class TableStatisticsServiceImpl extends ServiceImpl<TableStatisticsMapper, TableStatistics> implements ITableStatisticsService {

    public List<TableStatistics> ininTableStatistics(){
        return this.baseMapper.ininTableStatistics();
    }

}
