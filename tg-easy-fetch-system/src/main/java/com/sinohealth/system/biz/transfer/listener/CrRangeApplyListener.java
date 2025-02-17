package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.utils.SpringContextUtils;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.rangepreset.dao.RangeTemplatePresetDAO;
import com.sinohealth.system.biz.rangepreset.domain.RangeTemplatePreset;
import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDataRangeTemplate;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.service.DataRangeTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-07-09 14:02
 */

@Slf4j
public class CrRangeApplyListener extends CrFlowApplyListener {

    private final DataRangeTemplateService dataRangeTemplateService;
    private final RangeTemplatePresetDAO rangeTemplatePresetDAO;


    private final Map<String, Integer> flowIdMap = new ConcurrentHashMap<>();

    public CrRangeApplyListener(HttpServletRequest req,
                                DataSourceTransactionManager dataSourceTransactionManager,
                                TransactionDefinition transactionDefinition, Validator validator) {
        super(req, dataSourceTransactionManager, transactionDefinition, validator);


        this.dataRangeTemplateService = SpringContextUtils.getBean(DataRangeTemplateService.class);
        this.rangeTemplatePresetDAO = SpringContextUtils.getBean(RangeTemplatePresetDAO.class);
    }


    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        handleTransaction(() ->
                this.saveCommonApply(this::handleRangeApplyDetail, RedisKeys.RangeApply.TRANS_APPLY_MAP)
        );
        list.clear();
    }

    private void handleRangeApplyDetail(CrApplyVO vo, TgTemplateInfo template, TgApplicationInfo info) {
        super.handleFlowApplyDetail(vo, template, info);

        // 通用模板 设置工作流
        super.fillFlowId(vo.getFlowName(), template, info);

        // 填入自定义列id
        for (ApplicationGranularityDto dto : info.getGranularity()) {
            if (Objects.equals(dto.getGranularity(), FieldGranularityEnum.area.name())) {

                List<RangeTemplatePreset> rangeList = rangeTemplatePresetDAO.lambdaQuery()
                        .eq(RangeTemplatePreset::getName, vo.getAreaRangeTemp())
                        .list();
                if (rangeList.isEmpty()) {
                    throw new RuntimeException("未定义 自定义列预设 " + vo.getAreaRangeTemp());
                }

                if (rangeList.size() > 1) {
                    throw new RuntimeException("重复 自定义列预设 " + vo.getAreaRangeTemp());
                }
                RangeTemplatePreset range = rangeList.get(0);
                TgDataRangeTemplate rangeTemp = new TgDataRangeTemplate();
                rangeTemp.setDataRangeConfig(range.getGroupList());
                dataRangeTemplateService.save(rangeTemp);

                dto.setRangeTemplateId(rangeTemp.getId());
            }
        }
    }
}
