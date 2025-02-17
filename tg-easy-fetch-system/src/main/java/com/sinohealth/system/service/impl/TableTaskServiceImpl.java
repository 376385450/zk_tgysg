package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.domain.TableTask;
import com.sinohealth.system.mapper.TableTaskMapper;
import com.sinohealth.system.service.ITableTaskService;
import org.springframework.stereotype.Service;

/**
 * 表任务对象 Service接口
 *
 * @author liruifa
 * @date 2021-06-28
 */
@Service
public class TableTaskServiceImpl extends ServiceImpl<TableTaskMapper, TableTask> implements ITableTaskService {
}
