package com.sinohealth.system.dto.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Huangzk
 * @date 2021/8/6 9:34
 */
@Data
@Accessors(chain = true)
public class FileUploadRespVo {
    /**
     * 绝对路径
     */
    private String absolutePath;
}
