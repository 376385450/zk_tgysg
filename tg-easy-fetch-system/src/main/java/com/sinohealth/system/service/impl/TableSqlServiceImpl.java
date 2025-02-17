package com.sinohealth.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.Page;
import com.sinohealth.system.domain.TableSql;
import com.sinohealth.system.mapper.TableSqlMapper;
import com.sinohealth.system.service.ITableSqlService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author dataplatform
 * @date 2021-05-07
 */
@Service
public class TableSqlServiceImpl extends ServiceImpl<TableSqlMapper, TableSql> implements ITableSqlService {


}
