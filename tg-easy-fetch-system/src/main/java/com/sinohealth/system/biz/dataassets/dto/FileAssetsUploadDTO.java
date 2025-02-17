package com.sinohealth.system.biz.dataassets.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 16:09
 */
@Data
@Accessors(chain = true)
public class FileAssetsUploadDTO {
    private String name;
    private String path;
}
