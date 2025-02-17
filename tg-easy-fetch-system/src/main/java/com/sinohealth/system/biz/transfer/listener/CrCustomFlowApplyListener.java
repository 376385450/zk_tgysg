package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.system.biz.application.dto.TopSettingApplyDto;
import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.Map;

/**
 * Sheet： 通用
 *
 * @author Kuangcp
 * 2024-07-25 11:18
 */
@Slf4j
public class CrCustomFlowApplyListener extends CrFlowApplyListener {
    public CrCustomFlowApplyListener(HttpServletRequest req,
                                     DataSourceTransactionManager dataSourceTransactionManager,
                                     TransactionDefinition transactionDefinition, Validator validator) {
        super(req, dataSourceTransactionManager, transactionDefinition, validator);
    }

//    @Override
//    public void doAfterAllAnalysed(AnalysisContext context) {
//        handleTransaction(() ->
//                this.saveCommonApply(this::handleFlowApplyDetail, RedisKeys.NormalCustomApply.TRANS_APPLY_MAP)
//        );
//        list.clear();
//    }


    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);

        Map<Integer, String> headStrMap = ConverterUtils.convertToStringMap(headMap, context);
        log.info("{}: headStrMap={}", context.readSheetHolder().getSheetName(), headStrMap);
    }


    @Override
    String getKey() {
        return RedisKeys.NormalCustomApply.TRANS_APPLY_MAP;
    }

    /**
     * @see CrFlowApplyListener#doAfterAllAnalysed
     */
    @Override
    public void handleFlowApplyDetail(CrApplyVO vo, TgTemplateInfo template, TgApplicationInfo info) {
        super.handleFlowApplyDetail(vo, template, info);

        info.setExportProjectName(true);
        // 使用原始字段顺序
//        info.setRelateDict(false);

        TopSettingApplyDto dto = new TopSettingApplyDto();
        dto.setEnable(false);
        info.setTopSetting(dto);

        // 通用模板 设置工作流
        super.fillFlowId(vo.getFlowName(), template, info);
    }
}
