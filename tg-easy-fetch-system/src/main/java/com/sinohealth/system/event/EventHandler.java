package com.sinohealth.system.event;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Author Rudolph
 * @Date 2022-12-01 14:32
 * @Desc
 */
@Component
@Slf4j
public class EventHandler {

    @Autowired
    TgDocInfoMapper tgDocInfoMapper;

    @EventListener
    public void handleDocEvent(DocRecordEvent docRecordEvent) throws InterruptedException {
        log.info("事件响应:{}", docRecordEvent.getEventComment());

        UpdateWrapper<TgDocInfo> updateWrapper = new UpdateWrapper<>();

        if (CommonConstants.APPLY_TIMES.equals(docRecordEvent.getEventCode())) {
            updateWrapper.setSql("apply_times = apply_times + 1");
        }

        if (CommonConstants.SUCCESSFUL_APPLY_TIMES.equals(docRecordEvent.getEventCode())) {
            updateWrapper.setSql("successful_apply_times = successful_apply_times + 1");
        }

        if (CommonConstants.READ_TIMES.equals(docRecordEvent.getEventCode())) {
            updateWrapper.setSql("read_times = read_times + 1");
        }

        if (CommonConstants.PDF_DOWNLOAD_TIMES.equals(docRecordEvent.getEventCode())) {
            updateWrapper.setSql("pdf_download_times = pdf_download_times + 1");
        }

        if (CommonConstants.SOURCE_FILE_DOWNLOAD_TIMES.equals(docRecordEvent.getEventCode())) {
            updateWrapper.setSql("source_file_download_times = source_file_download_times + 1");
        }

        updateWrapper.eq("id", docRecordEvent.getDocId());

        tgDocInfoMapper.update(null, updateWrapper);

    }
}
