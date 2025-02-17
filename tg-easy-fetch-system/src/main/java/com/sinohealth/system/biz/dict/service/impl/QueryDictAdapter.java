package com.sinohealth.system.biz.dict.service.impl;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.dto.request.TryRunSQLRequest;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-03 14:05
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class QueryDictAdapter {

    private static final int maxRow = 2000;

    private final TgCkProviderMapper ckProviderMapper;
    /**
     * 依据SQL得到数据
     *
     * @param sqlRequest 注意limit限制
     */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public AjaxResult<List<BizDataDictValDTO>> tryRun(TryRunSQLRequest sqlRequest) {
        try {
            String sql = sqlRequest.getSql();
            ApplicationSqlUtil.checkSql(sql);

            if (Objects.nonNull(sqlRequest.getLimit())) {
                sqlRequest.setLimit(Math.min(sqlRequest.getLimit(), maxRow + 1));
                sql += " limit " + sqlRequest.getLimit();
            }
            List<LinkedHashMap<String, Object>> dataMap = ckProviderMapper.selectAllDataFromCk(sql);
            if (CollectionUtils.isEmpty(dataMap)) {
                return AjaxResult.error("无数据");
            }

            List<BizDataDictValDTO> result = dataMap.stream()
                    .filter(Objects::nonNull)
                    .map(v -> Optional.of(v.values())
                            .map(Collection::iterator)
                            .map(Iterator::next)
                            .map(Object::toString)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(v -> {
                        BizDataDictValDTO dto = new BizDataDictValDTO();
                        dto.setVal(v);
                        return dto;
                    }).collect(Collectors.toList());
            return AjaxResult.success(result);
        } catch (CustomException ce) {
            log.error("", ce);
            return AjaxResult.error(ce.getMessage());
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("SQL执行异常： " + e.getMessage());
        }
    }

}
