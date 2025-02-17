package com.sinohealth.system.domain.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgUploadedFileDim;
import lombok.Data;

@Data
public class TgDocVersionInfoVo {
    private Long docId;
    private TgDocInfo currentDocInfo;
    private Page<TgUploadedFileDim> versionInfo;
}
