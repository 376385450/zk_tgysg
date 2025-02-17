package com.sinohealth.system.biz.dataassets.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-07-17 16:04
 */
@Data
public class AssetsCompareFilePageDTO {
    private Long id;
    private String prodCode;
    private String dataPeriod;
    private String newFileName;
    private String oldFileName;
    private String state;
    private String creator;
    private LocalDateTime createTime;
}
