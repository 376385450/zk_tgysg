package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.TgDeliverEmailRecordDAO;
import com.sinohealth.system.domain.TgDeliverEmailRecord;
import com.sinohealth.system.mapper.TgDeliverEmailRecordMapper;
import org.springframework.stereotype.Repository;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 13:59
 */
@Repository
public class TgDeliverEmailRecordDAOImpl extends ServiceImpl<TgDeliverEmailRecordMapper, TgDeliverEmailRecord> implements TgDeliverEmailRecordDAO {
}
