package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-22 15:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsCompareInvokeRequest {

    @NotNull(message = "对比id为空")
    private Long compareId;
    @NotNull(message = "资产id为空")
    private Long assetsId;

    @NotBlank(message = "需求名为空")
    private String projectName;
    @NotBlank(message = "历史版本路径")
    private String oldPath;
    @NotBlank(message = "新版本路径")
    private String newPath;

    private String callbackUrl;

    /**
     * 纯文件对比 需要使用
     */
    private String resultDir;
}
