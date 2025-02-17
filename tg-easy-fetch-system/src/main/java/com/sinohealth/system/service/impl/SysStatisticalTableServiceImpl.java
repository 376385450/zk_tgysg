package com.sinohealth.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.Page;
import com.sinohealth.system.domain.SysStatisticalTable;
import com.sinohealth.system.mapper.SysStatisticalTableMapper;
import com.sinohealth.system.service.ISysStatisticalTableService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计数据库中间Service业务层处理
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Service
public class SysStatisticalTableServiceImpl extends ServiceImpl<SysStatisticalTableMapper, SysStatisticalTable> implements ISysStatisticalTableService {


}
