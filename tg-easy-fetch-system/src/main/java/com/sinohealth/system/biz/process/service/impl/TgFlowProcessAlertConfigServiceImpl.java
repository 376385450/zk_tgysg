package com.sinohealth.system.biz.process.service.impl;

import org.springframework.stereotype.Service;

import com.sinohealth.system.biz.process.dao.TgFlowProcessAlertConfigDAO;
import com.sinohealth.system.biz.process.service.TgFlowProcessAlertConfigService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TgFlowProcessAlertConfigServiceImpl implements TgFlowProcessAlertConfigService {
    private final TgFlowProcessAlertConfigDAO tgFlowProcessAlertConfigDAO;
}
